package com.wms.service.impl;

import com.wms.common.BusinessType;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.dto.InventoryQueryDTO;
import com.wms.dto.StocktakeResultDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.StocktakeOrder;
import com.wms.entity.StocktakeOrderDetail;
import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import com.wms.mapper.InventoryBatchMapper;
import com.wms.mapper.StocktakeOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.StocktakeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class StocktakeServiceImpl implements StocktakeService {

    @Autowired
    private StocktakeOrderMapper stocktakeOrderMapper;

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RedisLock redisLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStocktakeOrder(StocktakeOrder order, String operator) {
        order.setStocktakeNo(generateStocktakeNo());
        order.setStatus(1);
        order.setTotalItems(0);
        order.setTotalQuantity(BigDecimal.ZERO);
        order.setCountQuantity(BigDecimal.ZERO);
        order.setProfitQuantity(BigDecimal.ZERO);
        order.setLossQuantity(BigDecimal.ZERO);
        order.setCreateBy(operator);
        order.setUpdateBy(operator);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        int rows = stocktakeOrderMapper.insert(order);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "盘点单创建失败");
        }

        InventoryQueryDTO query = new InventoryQueryDTO();
        query.setWarehouseId(order.getWarehouseId());
        query.setAreaId(order.getAreaId());
        query.setOnlyHasStock(true);
        query.setPageNum(1);
        query.setPageSize(Integer.MAX_VALUE);

        List<InventoryBatch> batches = inventoryBatchMapper.selectList(query);
        if (batches != null && !batches.isEmpty()) {
            for (InventoryBatch batch : batches) {
                StocktakeOrderDetail detail = new StocktakeOrderDetail();
                detail.setStocktakeOrderId(order.getId());
                detail.setProductId(batch.getProductId());
                detail.setBatchNo(batch.getBatchNo());
                detail.setLocationId(batch.getLocationId());
                detail.setSystemQuantity(batch.getQuantity());
                detail.setFirstCount(BigDecimal.ZERO);
                detail.setSecondCount(BigDecimal.ZERO);
                detail.setFinalCount(BigDecimal.ZERO);
                detail.setDiffQuantity(BigDecimal.ZERO);
                detail.setUnit(batch.getUnit());
                detail.setIsCounted(0);
                detail.setProcessStatus(0);
                detail.setRemark("");
                detail.setCreateTime(new Date());
                detail.setUpdateTime(new Date());

                stocktakeOrderMapper.insertDetail(detail);
            }

            order.setTotalItems(batches.size());
            BigDecimal totalQty = batches.stream()
                    .map(InventoryBatch::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalQuantity(totalQty);
            stocktakeOrderMapper.updateById(order);
        }

        log.info("创建盘点单成功: stocktakeNo={}, operator={}", order.getStocktakeNo(), operator);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enterStocktakeResult(StocktakeResultDTO dto, String operator) {
        String lockKey = "stocktake:enter:" + dto.getStocktakeId();
        redisLock.executeWithLock(lockKey, () -> {
            StocktakeOrder order = stocktakeOrderMapper.selectById(dto.getStocktakeId());
            if (order == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "盘点单不存在");
            }
            if (order.getStatus() < 2 || order.getStatus() > 3) {
                if (order.getStatus() == 1) {
                    order.setStatus(2);
                    order.setConfirmTime(new Date());
                    order.setStartTime(new Date());
                    order.setUpdateBy(operator);
                    order.setUpdateTime(new Date());
                    stocktakeOrderMapper.updateById(order);
                } else {
                    throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "当前状态不允许录入盘点结果");
                }
            }

            BigDecimal totalCountQty = BigDecimal.ZERO;
            BigDecimal totalProfitQty = BigDecimal.ZERO;
            BigDecimal totalLossQty = BigDecimal.ZERO;

            for (StocktakeResultDTO.StocktakeDetailDTO detailDTO : dto.getDetails()) {
                StocktakeOrderDetail detail = stocktakeOrderMapper.selectDetailById(detailDTO.getDetailId());
                if (detail == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXIST, "盘点明细不存在");
                }
                if (!detail.getStocktakeOrderId().equals(dto.getStocktakeId())) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "盘点明细不属于当前盘点单");
                }
                if (detail.getIsCounted() == 1) {
                    throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "该明细已盘点，请勿重复录入");
                }

                detail.setFirstCount(detailDTO.getFirstCount());
                detail.setSecondCount(detailDTO.getSecondCount());
                detail.setFinalCount(detailDTO.getFinalCount());
                detail.setCounter(dto.getCounter() != null ? dto.getCounter() : operator);
                detail.setDiffReason(detailDTO.getDiffReason());

                stocktakeOrderMapper.updateDetailForCount(detail);

                BigDecimal diffQty = detailDTO.getFinalCount().subtract(detail.getSystemQuantity());
                totalCountQty = totalCountQty.add(detailDTO.getFinalCount());
                if (diffQty.compareTo(BigDecimal.ZERO) > 0) {
                    totalProfitQty = totalProfitQty.add(diffQty);
                } else if (diffQty.compareTo(BigDecimal.ZERO) < 0) {
                    totalLossQty = totalLossQty.add(diffQty.abs());
                }
            }

            stocktakeOrderMapper.addCountResult(dto.getStocktakeId(), totalCountQty, totalProfitQty, totalLossQty);

            StocktakeOrder updatedOrder = stocktakeOrderMapper.selectById(dto.getStocktakeId());
            List<StocktakeOrderDetail> allDetails = stocktakeOrderMapper.selectDetailsByOrderId(dto.getStocktakeId());
            boolean allCounted = allDetails.stream().allMatch(d -> d.getIsCounted() == 1);

            if (allCounted) {
                updatedOrder.setStatus(3);
                updatedOrder.setUpdateBy(operator);
                updatedOrder.setUpdateTime(new Date());
                stocktakeOrderMapper.updateById(updatedOrder);

                boolean hasDiff = allDetails.stream()
                        .anyMatch(d -> d.getDiffType() != null && d.getDiffType() != 3);
                if (hasDiff) {
                    updatedOrder.setStatus(4);
                    stocktakeOrderMapper.updateById(updatedOrder);
                }
            }

            log.info("盘点结果录入成功: stocktakeId={}, count={}, operator={}",
                    dto.getStocktakeId(), dto.getDetails().size(), operator);
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processDiff(Long detailId, Integer processStatus, String processResult, String operator) {
        StocktakeOrderDetail detail = stocktakeOrderMapper.selectDetailById(detailId);
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "盘点明细不存在");
        }

        StocktakeOrder order = stocktakeOrderMapper.selectById(detail.getStocktakeOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "盘点单不存在");
        }
        if (order.getStatus() != 4) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "请先完成盘点录入");
        }
        if (detail.getDiffType() == null || detail.getDiffType() == 3) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "该明细无差异，无需处理");
        }

        stocktakeOrderMapper.updateDetailForProcess(detailId, processStatus, processResult, operator);

        if (processStatus == 2) {
            InventoryBatch batch = inventoryBatchMapper.selectForUpdate(
                    order.getWarehouseId(),
                    detail.getProductId(),
                    detail.getBatchNo(),
                    detail.getLocationId()
            );

            if (detail.getDiffType() == 1 && detail.getDiffQuantity().compareTo(BigDecimal.ZERO) > 0) {
                if (batch != null) {
                    inventoryBatchMapper.addQuantity(batch.getId(), detail.getDiffQuantity());
                } else {
                    batch = new InventoryBatch();
                    batch.setWarehouseId(order.getWarehouseId());
                    batch.setLocationId(detail.getLocationId());
                    batch.setProductId(detail.getProductId());
                    batch.setBatchNo(detail.getBatchNo());
                    batch.setQuantity(detail.getDiffQuantity());
                    batch.setAvailableQuantity(detail.getDiffQuantity());
                    batch.setLockedQuantity(BigDecimal.ZERO);
                    batch.setFrozenQuantity(BigDecimal.ZERO);
                    batch.setUnit(detail.getUnit());
                    batch.setInboundDate(new Date());
                    batch.setInventoryStatus(1);
                    batch.setInspectionStatus(2);
                    batch.setCreateBy(operator);
                    batch.setUpdateBy(operator);
                    batch.setCreateTime(new Date());
                    batch.setUpdateTime(new Date());
                    inventoryBatchMapper.insert(batch);
                }
            } else if (detail.getDiffType() == 2 && detail.getDiffQuantity().compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal reduceQty = detail.getDiffQuantity().abs();
                if (batch != null) {
                    int rows = inventoryBatchMapper.reduceQuantity(batch.getId(), reduceQty);
                    if (rows != 1) {
                        throw new BusinessException(ResultCode.REDUCE_FAILED, "库存扣减失败，库存不足");
                    }
                }
            }
        }

        log.info("盘点差异处理: detailId={}, status={}, operator={}", detailId, processStatus, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStocktakeComplete(Long stocktakeId, String operator) {
        String lockKey = "stocktake:complete:" + stocktakeId;
        redisLock.executeWithLock(lockKey, () -> {
            StocktakeOrder order = stocktakeOrderMapper.selectById(stocktakeId);
            if (order == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "盘点单不存在");
            }

            List<StocktakeOrderDetail> details = stocktakeOrderMapper.selectDetailsByOrderId(stocktakeId);

            for (StocktakeOrderDetail detail : details) {
                if (detail.getDiffType() != null && detail.getDiffType() != 3 && detail.getProcessStatus() != 2) {
                    throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR,
                            "还有未处理的差异明细，请先处理差异");
                }
            }

            for (StocktakeOrderDetail detail : details) {
                if (detail.getDiffType() != null && detail.getDiffType() != 3) {
                    if (detail.getDiffQuantity().compareTo(BigDecimal.ZERO) > 0) {
                        inventoryService.stockIn(
                                order.getWarehouseId(),
                                detail.getLocationId(),
                                detail.getProductId(),
                                detail.getBatchNo(),
                                null,
                                detail.getDiffQuantity(),
                                detail.getUnit(),
                                null,
                                null,
                                null,
                                order.getStocktakeNo(),
                                operator,
                                "盘盈入库"
                        );
                    } else if (detail.getDiffQuantity().compareTo(BigDecimal.ZERO) < 0) {
                        InventoryBatch batch = inventoryBatchMapper.selectForUpdate(
                                order.getWarehouseId(),
                                detail.getProductId(),
                                detail.getBatchNo(),
                                detail.getLocationId()
                        );
                        if (batch != null) {
                            inventoryService.reduceInventory(
                                    batch.getId(),
                                    detail.getDiffQuantity().abs(),
                                    order.getStocktakeNo(),
                                    operator
                            );
                        }
                    }
                }
            }

            order.setStatus(5);
            order.setFinishTime(new Date());
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            stocktakeOrderMapper.updateById(order);

            log.info("盘点单完成: stocktakeNo={}, operator={}", order.getStocktakeNo(), operator);
            return null;
        });
    }

    @Override
    public PageResult<StocktakeOrder> queryStocktakeOrders(PageQuery query, Integer stocktakeType,
                                                           Integer status, Long warehouseId,
                                                           Long areaId, String handler) {
        List<StocktakeOrder> list = stocktakeOrderMapper.selectList(query, stocktakeType, status,
                warehouseId, areaId, handler);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public StocktakeOrder getById(Long id) {
        return stocktakeOrderMapper.selectById(id);
    }

    @Override
    public StocktakeOrder getByNo(String stocktakeNo) {
        return stocktakeOrderMapper.selectByNo(stocktakeNo);
    }

    @Override
    public List<StocktakeOrderDetail> getDetailsByOrderId(Long orderId) {
        return stocktakeOrderMapper.selectDetailsByOrderId(orderId);
    }

    private String generateStocktakeNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "PD" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

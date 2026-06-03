package com.wms.service.impl;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.entity.ReturnOrder;
import com.wms.entity.ReturnOrderDetail;
import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import com.wms.mapper.ReturnOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.ReturnOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReturnOrderServiceImpl implements ReturnOrderService {

    @Autowired
    private ReturnOrderMapper returnOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RedisLock redisLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturnOrder(ReturnOrder order, List<ReturnOrderDetail> details, String operator) {
        order.setReturnNo(generateReturnNo());
        order.setStatus(1);
        order.setReceiveTime(new Date());
        order.setActualQuantity(BigDecimal.ZERO);
        order.setCreateBy(operator);
        order.setUpdateBy(operator);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        BigDecimal totalQty = details.stream()
                .map(ReturnOrderDetail::getReturnQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalQuantity(totalQty);

        int rows = returnOrderMapper.insert(order);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "退货单创建失败");
        }

        for (ReturnOrderDetail detail : details) {
            detail.setReturnOrderId(order.getId());
            detail.setActualQuantity(BigDecimal.ZERO);
            detail.setInspectionResult(3);
            detail.setCreateTime(new Date());
            detail.setUpdateTime(new Date());
            returnOrderMapper.insertDetail(detail);
        }

        log.info("创建退货单成功: returnNo={}, operator={}", order.getReturnNo(), operator);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doReturnInspection(Long detailId, Integer inspectionResult,
                                   BigDecimal actualQuantity, String operator) {
        ReturnOrderDetail detail = returnOrderMapper.selectDetailById(detailId);
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "退货明细不存在");
        }

        ReturnOrder order = returnOrderMapper.selectById(detail.getReturnOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "退货单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "当前状态不允许质检");
        }

        detail.setInspectionResult(inspectionResult);
        detail.setActualQuantity(actualQuantity);
        detail.setUpdateTime(new Date());

        int rows = returnOrderMapper.updateDetail(detail);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "质检结果更新失败");
        }

        if (order.getStatus() == 1) {
            order.setStatus(2);
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            returnOrderMapper.updateById(order);
        }

        List<ReturnOrderDetail> allDetails = returnOrderMapper.selectDetailsByOrderId(order.getId());
        boolean allInspected = allDetails.stream()
                .allMatch(d -> d.getInspectionResult() != null && d.getInspectionResult() != 3);

        if (allInspected) {
            order.setStatus(3);
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            returnOrderMapper.updateById(order);
        }

        log.info("退货质检完成: detailId={}, result={}, operator={}", detailId, inspectionResult, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnToStock(Long detailId, Long locationId, String operator) {
        String lockKey = "return:stock:" + detailId;
        redisLock.executeWithLock(lockKey, () -> {
            ReturnOrderDetail detail = returnOrderMapper.selectDetailById(detailId);
            if (detail == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "退货明细不存在");
            }

            ReturnOrder order = returnOrderMapper.selectById(detail.getReturnOrderId());
            if (order == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "退货单不存在");
            }
            if (order.getStatus() != 3) {
                throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "请先完成质检");
            }
            if (detail.getInspectionResult() != 1) {
                throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "质检不合格的商品不能入库");
            }
            if (detail.getActualQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "实际入库数量必须大于0");
            }
            if (detail.getLocationId() != null) {
                throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "该明细已入库，请勿重复操作");
            }

            if (!inventoryService.checkLocationCapacity(locationId, detail.getActualQuantity())) {
                throw new BusinessException(ResultCode.LOCATION_CAPACITY_NOT_ENOUGH);
            }

            detail.setLocationId(locationId);
            detail.setUpdateTime(new Date());

            int rows = returnOrderMapper.updateDetail(detail);
            if (rows != 1) {
                throw new BusinessException(ResultCode.DATABASE_ERROR, "退货入库失败");
            }

            returnOrderMapper.addActualQuantity(order.getId(), detail.getActualQuantity());

            inventoryService.stockIn(
                    order.getWarehouseId(),
                    locationId,
                    detail.getProductId(),
                    detail.getBatchNo(),
                    null,
                    detail.getActualQuantity(),
                    detail.getUnit(),
                    null,
                    null,
                    null,
                    order.getReturnNo(),
                    operator,
                    "退货入库"
            );

            log.info("退货入库成功: detailId={}, locationId={}, quantity={}, operator={}",
                    detailId, locationId, detail.getActualQuantity(), operator);
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturnComplete(Long returnOrderId, String operator) {
        ReturnOrder order = returnOrderMapper.selectById(returnOrderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "退货单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "请先完成质检和入库");
        }

        List<ReturnOrderDetail> details = returnOrderMapper.selectDetailsByOrderId(returnOrderId);
        for (ReturnOrderDetail detail : details) {
            if (detail.getInspectionResult() == 1 && detail.getLocationId() == null) {
                throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR,
                        "还有质检合格的商品未入库，请先完成入库");
            }
        }

        order.setStatus(4);
        order.setCompleteTime(new Date());
        order.setUpdateBy(operator);
        order.setUpdateTime(new Date());

        int rows = returnOrderMapper.updateById(order);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "退货单完成失败");
        }

        log.info("退货单完成: returnNo={}, operator={}", order.getReturnNo(), operator);
    }

    @Override
    public PageResult<ReturnOrder> queryReturnOrders(PageQuery query, Integer returnType,
                                                      Integer status, Long warehouseId,
                                                      String originalShipmentNo) {
        List<ReturnOrder> list = returnOrderMapper.selectList(query, returnType, status,
                warehouseId, originalShipmentNo);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public ReturnOrder getById(Long id) {
        return returnOrderMapper.selectById(id);
    }

    @Override
    public ReturnOrder getByNo(String returnNo) {
        return returnOrderMapper.selectByNo(returnNo);
    }

    @Override
    public List<ReturnOrderDetail> getDetailsByOrderId(Long orderId) {
        return returnOrderMapper.selectDetailsByOrderId(orderId);
    }

    private String generateReturnNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "RT" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

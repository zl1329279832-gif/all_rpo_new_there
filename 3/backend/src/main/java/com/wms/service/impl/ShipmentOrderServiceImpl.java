package com.wms.service.impl;

import com.wms.common.OutboundStrategy;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.dto.ShipmentOrderCreateDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.ShipmentAllocateDetail;
import com.wms.entity.ShipmentOrder;
import com.wms.entity.ShipmentOrderDetail;
import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import com.wms.mapper.ShipmentOrderMapper;
import com.wms.service.InventoryService;
import com.wms.service.ShipmentOrderService;
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
public class ShipmentOrderServiceImpl implements ShipmentOrderService {

    @Autowired
    private ShipmentOrderMapper shipmentOrderMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RedisLock redisLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShipmentOrder(ShipmentOrderCreateDTO dto, String operator) {
        ShipmentOrder order = new ShipmentOrder();
        order.setShipmentNo(generateShipmentNo());
        order.setShipmentType(dto.getShipmentType());
        order.setWarehouseId(dto.getWarehouseId());
        order.setCustomerName(dto.getCustomerName());
        order.setOrderStatus(1);
        order.setSourceOrderNo(dto.getSourceOrderNo());
        order.setRemark(dto.getRemark());
        order.setCreateBy(operator);
        order.setUpdateBy(operator);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (ShipmentOrderCreateDTO.ShipmentDetailDTO detailDTO : dto.getDetails()) {
            totalQuantity = totalQuantity.add(detailDTO.getPlanQuantity());
        }
        order.setTotalQuantity(totalQuantity);
        order.setPickedQuantity(BigDecimal.ZERO);
        order.setReviewedQuantity(BigDecimal.ZERO);
        order.setActualQuantity(BigDecimal.ZERO);

        int rows = shipmentOrderMapper.insert(order);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "出库单创建失败");
        }

        for (ShipmentOrderCreateDTO.ShipmentDetailDTO detailDTO : dto.getDetails()) {
            ShipmentOrderDetail detail = new ShipmentOrderDetail();
            detail.setShipmentOrderId(order.getId());
            detail.setProductId(detailDTO.getProductId());
            detail.setPlanQuantity(detailDTO.getPlanQuantity());
            detail.setAllocatedQuantity(BigDecimal.ZERO);
            detail.setPickedQuantity(BigDecimal.ZERO);
            detail.setReviewedQuantity(BigDecimal.ZERO);
            detail.setActualQuantity(BigDecimal.ZERO);
            detail.setUnit(detailDTO.getUnit());
            detail.setOutboundStrategy(detailDTO.getOutboundStrategy());
            detail.setPrice(detailDTO.getPrice());
            detail.setRemark(detailDTO.getRemark());
            detail.setCreateTime(new Date());
            detail.setUpdateTime(new Date());

            shipmentOrderMapper.insertDetail(detail);
        }

        log.info("创建出库单成功: shipmentNo={}, operator={}", order.getShipmentNo(), operator);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocateInventory(Long shipmentOrderId, String operator) {
        String lockKey = "shipment:allocate:" + shipmentOrderId;
        redisLock.executeWithLock(lockKey, () -> {
            ShipmentOrder order = shipmentOrderMapper.selectById(shipmentOrderId);
            if (order == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "出库单不存在");
            }
            if (order.getOrderStatus() != 1 && order.getOrderStatus() != 2) {
                throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "当前状态不允许分配库存");
            }

            List<ShipmentOrderDetail> details = shipmentOrderMapper.selectDetailsByOrderId(shipmentOrderId);

            shipmentOrderMapper.deleteAllocateDetailsByOrderId(shipmentOrderId);

            for (ShipmentOrderDetail detail : details) {
                OutboundStrategy strategy = OutboundStrategy.fromCode(detail.getOutboundStrategy());
                String specifyBatchNo = null;
                if (strategy == OutboundStrategy.SPECIFY_BATCH) {
                    ShipmentOrderCreateDTO.ShipmentDetailDTO temp = new ShipmentOrderCreateDTO.ShipmentDetailDTO();
                    specifyBatchNo = temp.getSpecifyBatchNo();
                }

                List<InventoryBatch> allocatedBatches = inventoryService.allocateForOutbound(
                        order.getWarehouseId(),
                        detail.getProductId(),
                        detail.getPlanQuantity(),
                        strategy.getCode(),
                        specifyBatchNo
                );

                BigDecimal totalAllocated = BigDecimal.ZERO;
                for (InventoryBatch batch : allocatedBatches) {
                    inventoryService.lockInventory(batch.getId(), batch.getQuantity(),
                            order.getShipmentNo(), operator);

                    ShipmentAllocateDetail allocateDetail = new ShipmentAllocateDetail();
                    allocateDetail.setShipmentOrderId(order.getId());
                    allocateDetail.setShipmentDetailId(detail.getId());
                    allocateDetail.setProductId(detail.getProductId());
                    allocateDetail.setBatchNo(batch.getBatchNo());
                    allocateDetail.setLocationId(batch.getLocationId());
                    allocateDetail.setAllocateQuantity(batch.getQuantity());
                    allocateDetail.setPickedQuantity(BigDecimal.ZERO);
                    allocateDetail.setReviewedQuantity(BigDecimal.ZERO);
                    allocateDetail.setUnit(batch.getUnit());
                    allocateDetail.setProduceDate(batch.getProduceDate());
                    allocateDetail.setExpireDate(batch.getExpireDate());
                    allocateDetail.setCostPrice(batch.getCostPrice());
                    allocateDetail.setIsPicked(0);
                    allocateDetail.setIsReviewed(0);
                    allocateDetail.setCreateTime(new Date());
                    allocateDetail.setUpdateTime(new Date());

                    shipmentOrderMapper.insertAllocateDetail(allocateDetail);
                    totalAllocated = totalAllocated.add(batch.getQuantity());
                }

                detail.setAllocatedQuantity(totalAllocated);
                detail.setUpdateTime(new Date());
                shipmentOrderMapper.updateDetail(detail);
            }

            order.setOrderStatus(2);
            order.setAllocateTime(new Date());
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            shipmentOrderMapper.updateById(order);

            log.info("出库单库存分配成功: shipmentNo={}, operator={}", order.getShipmentNo(), operator);
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelShipment(Long shipmentOrderId, String cancelReason, String operator) {
        String lockKey = "shipment:cancel:" + shipmentOrderId;
        redisLock.executeWithLock(lockKey, () -> {
            ShipmentOrder order = shipmentOrderMapper.selectById(shipmentOrderId);
            if (order == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "出库单不存在");
            }
            if (order.getOrderStatus() == 7 || order.getOrderStatus() == 8) {
                throw new BusinessException(ResultCode.OUTBOUND_CANCELLED, "出库单已完成或已取消");
            }

            List<ShipmentAllocateDetail> allocateDetails = shipmentOrderMapper.selectAllocateDetailsByOrderId(shipmentOrderId);
            for (ShipmentAllocateDetail allocateDetail : allocateDetails) {
                if (allocateDetail.getPickedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    InventoryBatch batch = inventoryService.getById(allocateDetail.getId());
                    if (batch != null) {
                        inventoryService.reduceLockedInventory(batch.getId(), allocateDetail.getPickedQuantity(),
                                order.getShipmentNo(), operator);
                    }
                }
                BigDecimal unlockQty = allocateDetail.getAllocateQuantity().subtract(allocateDetail.getPickedQuantity());
                if (unlockQty.compareTo(BigDecimal.ZERO) > 0) {
                    InventoryBatch batch = inventoryService.getById(allocateDetail.getId());
                    if (batch != null) {
                        inventoryService.unlockInventory(batch.getId(), unlockQty,
                                order.getShipmentNo(), operator);
                    }
                }
            }

            order.setOrderStatus(8);
            order.setCancelTime(new Date());
            order.setCancelReason(cancelReason);
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            shipmentOrderMapper.updateById(order);

            log.info("出库单取消成功: shipmentNo={}, operator={}", order.getShipmentNo(), operator);
            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmShipmentComplete(Long shipmentOrderId, String operator) {
        ShipmentOrder order = shipmentOrderMapper.selectById(shipmentOrderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "出库单不存在");
        }
        if (order.getOrderStatus() != 6) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "请先完成复核");
        }

        List<ShipmentAllocateDetail> allocateDetails = shipmentOrderMapper.selectAllocateDetailsByOrderId(shipmentOrderId);
        for (ShipmentAllocateDetail allocateDetail : allocateDetails) {
            BigDecimal reduceQty = allocateDetail.getAllocateQuantity().subtract(allocateDetail.getPickedQuantity());
            if (reduceQty.compareTo(BigDecimal.ZERO) > 0) {
                InventoryBatch batch = inventoryService.getById(allocateDetail.getId());
                if (batch != null) {
                    inventoryService.unlockInventory(batch.getId(), reduceQty,
                            order.getShipmentNo(), operator);
                }
            }
        }

        order.setOrderStatus(7);
        order.setShipmentTime(new Date());
        order.setActualQuantity(order.getPickedQuantity());
        order.setUpdateBy(operator);
        order.setUpdateTime(new Date());
        shipmentOrderMapper.updateById(order);

        log.info("出库单完成: shipmentNo={}, operator={}", order.getShipmentNo(), operator);
    }

    @Override
    public PageResult<ShipmentOrder> queryShipmentOrders(PageQuery query, Integer shipmentType,
                                                         Integer orderStatus, Long warehouseId,
                                                         String customerName, Date startTime, Date endTime) {
        List<ShipmentOrder> list = shipmentOrderMapper.selectList(query, shipmentType, orderStatus,
                warehouseId, customerName, startTime, endTime);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public ShipmentOrder getById(Long id) {
        return shipmentOrderMapper.selectById(id);
    }

    @Override
    public ShipmentOrder getByNo(String shipmentNo) {
        return shipmentOrderMapper.selectByNo(shipmentNo);
    }

    @Override
    public List<ShipmentOrderDetail> getDetailsByOrderId(Long orderId) {
        return shipmentOrderMapper.selectDetailsByOrderId(orderId);
    }

    @Override
    public List<ShipmentAllocateDetail> getAllocateDetailsByOrderId(Long orderId) {
        return shipmentOrderMapper.selectAllocateDetailsByOrderId(orderId);
    }

    private String generateShipmentNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "CK" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

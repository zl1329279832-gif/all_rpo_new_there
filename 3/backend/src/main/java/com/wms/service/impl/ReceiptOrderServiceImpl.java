package com.wms.service.impl;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.dto.ReceiptOrderCreateDTO;
import com.wms.entity.Location;
import com.wms.entity.ReceiptOrder;
import com.wms.entity.ReceiptOrderDetail;
import com.wms.entity.Supplier;
import com.wms.exception.BusinessException;
import com.wms.mapper.LocationMapper;
import com.wms.mapper.ReceiptOrderMapper;
import com.wms.mapper.SupplierMapper;
import com.wms.service.InventoryService;
import com.wms.service.ReceiptOrderService;
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
public class ReceiptOrderServiceImpl implements ReceiptOrderService {

    @Autowired
    private ReceiptOrderMapper receiptOrderMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReceiptOrder(ReceiptOrderCreateDTO dto, String operator) {
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(dto.getSupplierId());
            if (supplier == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "供应商不存在");
            }
        }

        ReceiptOrder order = new ReceiptOrder();
        order.setReceiptNo(generateReceiptNo());
        order.setReceiptType(dto.getReceiptType());
        order.setWarehouseId(dto.getWarehouseId());
        order.setSupplierId(dto.getSupplierId());
        order.setOrderStatus(1);
        order.setSourceOrderNo(dto.getSourceOrderNo());
        order.setRemark(dto.getRemark());
        order.setCreateBy(operator);
        order.setUpdateBy(operator);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (ReceiptOrderCreateDTO.ReceiptDetailDTO detailDTO : dto.getDetails()) {
            totalQuantity = totalQuantity.add(detailDTO.getPlanQuantity());
        }
        order.setTotalQuantity(totalQuantity);
        order.setActualQuantity(BigDecimal.ZERO);
        order.setQualifiedQuantity(BigDecimal.ZERO);
        order.setUnqualifiedQuantity(BigDecimal.ZERO);

        int rows = receiptOrderMapper.insert(order);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "入库单创建失败");
        }

        for (ReceiptOrderCreateDTO.ReceiptDetailDTO detailDTO : dto.getDetails()) {
            ReceiptOrderDetail detail = new ReceiptOrderDetail();
            detail.setReceiptOrderId(order.getId());
            detail.setProductId(detailDTO.getProductId());
            detail.setBatchNo(detailDTO.getBatchNo());
            detail.setPlanQuantity(detailDTO.getPlanQuantity());
            detail.setArrivalQuantity(BigDecimal.ZERO);
            detail.setQualifiedQuantity(BigDecimal.ZERO);
            detail.setUnqualifiedQuantity(BigDecimal.ZERO);
            detail.setActualQuantity(BigDecimal.ZERO);
            detail.setUnit(detailDTO.getUnit());
            detail.setProduceDate(detailDTO.getProduceDate());
            detail.setExpireDate(detailDTO.getExpireDate());
            detail.setCostPrice(detailDTO.getCostPrice());
            detail.setRemark(detailDTO.getRemark());
            detail.setCreateTime(new Date());
            detail.setUpdateTime(new Date());

            receiptOrderMapper.insertDetail(detail);
        }

        log.info("创建入库单成功: receiptNo={}, operator={}", order.getReceiptNo(), operator);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmArrival(Long receiptOrderId, Date arrivalTime, String operator) {
        ReceiptOrder order = receiptOrderMapper.selectById(receiptOrderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "入库单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "当前状态不允许到货确认");
        }

        order.setOrderStatus(2);
        order.setArrivalTime(arrivalTime != null ? arrivalTime : new Date());
        order.setUpdateBy(operator);
        order.setUpdateTime(new Date());

        int rows = receiptOrderMapper.updateById(order);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "到货确认失败");
        }

        log.info("入库单到货确认成功: receiptNo={}, operator={}", order.getReceiptNo(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doInspection(Long detailId, BigDecimal arrivalQuantity, BigDecimal qualifiedQuantity,
                             BigDecimal unqualifiedQuantity, Integer inspectionResult,
                             String inspectionRemark, String operator) {
        ReceiptOrderDetail detail = receiptOrderMapper.selectDetailById(detailId);
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "入库单明细不存在");
        }

        ReceiptOrder order = receiptOrderMapper.selectById(detail.getReceiptOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "入库单不存在");
        }
        if (order.getOrderStatus() < 2 || order.getOrderStatus() > 3) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "当前状态不允许质检");
        }

        if (arrivalQuantity.compareTo(qualifiedQuantity.add(unqualifiedQuantity)) != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "到货数量必须等于合格数量加不合格数量");
        }

        detail.setArrivalQuantity(arrivalQuantity);
        detail.setQualifiedQuantity(qualifiedQuantity);
        detail.setUnqualifiedQuantity(unqualifiedQuantity);
        detail.setInspectionResult(inspectionResult);
        detail.setInspectionRemark(inspectionRemark);
        detail.setUpdateTime(new Date());

        int rows = receiptOrderMapper.updateDetailForInspection(detail);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "质检结果更新失败");
        }

        List<ReceiptOrderDetail> allDetails = receiptOrderMapper.selectDetailsByOrderId(order.getId());
        boolean allInspected = allDetails.stream()
                .allMatch(d -> d.getInspectionResult() != null);

        if (allInspected) {
            order.setOrderStatus(4);
            order.setInspectionTime(new Date());
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());

            BigDecimal totalQualified = allDetails.stream()
                    .map(ReceiptOrderDetail::getQualifiedQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalUnqualified = allDetails.stream()
                    .map(ReceiptOrderDetail::getUnqualifiedQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setQualifiedQuantity(totalQualified);
            order.setUnqualifiedQuantity(totalUnqualified);
            order.setActualQuantity(totalQualified);

            receiptOrderMapper.updateById(order);
        } else {
            if (order.getOrderStatus() == 2) {
                order.setOrderStatus(3);
                order.setUpdateBy(operator);
                order.setUpdateTime(new Date());
                receiptOrderMapper.updateById(order);
            }
        }

        log.info("入库质检完成: detailId={}, result={}, operator={}", detailId, inspectionResult, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignLocation(Long detailId, Long locationId, String operator) {
        ReceiptOrderDetail detail = receiptOrderMapper.selectDetailById(detailId);
        if (detail == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "入库单明细不存在");
        }

        ReceiptOrder order = receiptOrderMapper.selectById(detail.getReceiptOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "入库单不存在");
        }
        if (order.getOrderStatus() != 4) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "请先完成质检");
        }
        if (detail.getQualifiedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "合格数量为0，无需分配库位");
        }

        Location location = locationMapper.selectById(locationId);
        if (location == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "库位不存在");
        }
        if (!location.getWarehouseId().equals(order.getWarehouseId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "库位不属于当前仓库");
        }

        if (!inventoryService.checkLocationCapacity(locationId, detail.getQualifiedQuantity())) {
            throw new BusinessException(ResultCode.LOCATION_CAPACITY_NOT_ENOUGH);
        }

        detail.setLocationId(locationId);
        detail.setActualQuantity(detail.getQualifiedQuantity());
        detail.setUpdateTime(new Date());

        int rows = receiptOrderMapper.updateDetail(detail);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "库位分配失败");
        }

        List<ReceiptOrderDetail> allDetails = receiptOrderMapper.selectDetailsByOrderId(order.getId());
        boolean allAssigned = allDetails.stream()
                .filter(d -> d.getQualifiedQuantity().compareTo(BigDecimal.ZERO) > 0)
                .allMatch(d -> d.getLocationId() != null);

        if (allAssigned) {
            order.setOrderStatus(5);
            order.setUpdateBy(operator);
            order.setUpdateTime(new Date());
            receiptOrderMapper.updateById(order);
        }

        log.info("库位分配成功: detailId={}, locationId={}, operator={}", detailId, locationId, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceiptComplete(Long receiptOrderId, String operator) {
        ReceiptOrder order = receiptOrderMapper.selectById(receiptOrderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "入库单不存在");
        }
        if (order.getOrderStatus() != 5) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "请先完成库位分配");
        }

        List<ReceiptOrderDetail> details = receiptOrderMapper.selectDetailsByOrderId(receiptOrderId);

        order.setOrderStatus(6);
        order.setUpdateBy(operator);
        order.setUpdateTime(new Date());
        receiptOrderMapper.updateById(order);

        for (ReceiptOrderDetail detail : details) {
            if (detail.getQualifiedQuantity().compareTo(BigDecimal.ZERO) > 0
                    && detail.getLocationId() != null) {
                inventoryService.stockIn(
                        order.getWarehouseId(),
                        detail.getLocationId(),
                        detail.getProductId(),
                        detail.getBatchNo(),
                        order.getSupplierId(),
                        detail.getQualifiedQuantity(),
                        detail.getUnit(),
                        detail.getProduceDate(),
                        detail.getExpireDate(),
                        detail.getCostPrice(),
                        order.getReceiptNo(),
                        operator,
                        detail.getRemark()
                );
            }
        }

        order.setOrderStatus(7);
        order.setCompleteTime(new Date());
        order.setUpdateBy(operator);
        order.setUpdateTime(new Date());
        receiptOrderMapper.updateById(order);

        log.info("入库单完成: receiptNo={}, operator={}", order.getReceiptNo(), operator);
    }

    @Override
    public PageResult<ReceiptOrder> queryReceiptOrders(PageQuery query, Integer receiptType,
                                                       Integer orderStatus, Long warehouseId,
                                                       Long supplierId, Date startTime, Date endTime) {
        List<ReceiptOrder> list = receiptOrderMapper.selectList(query, receiptType, orderStatus,
                warehouseId, supplierId, startTime, endTime);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public ReceiptOrder getById(Long id) {
        return receiptOrderMapper.selectById(id);
    }

    @Override
    public ReceiptOrder getByNo(String receiptNo) {
        return receiptOrderMapper.selectByNo(receiptNo);
    }

    @Override
    public List<ReceiptOrderDetail> getDetailsByOrderId(Long orderId) {
        return receiptOrderMapper.selectDetailsByOrderId(orderId);
    }

    private String generateReceiptNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "RK" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

package com.wms.service.impl;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.dto.InventoryQueryDTO;
import com.wms.entity.InventoryAlert;
import com.wms.entity.InventoryBatch;
import com.wms.entity.Product;
import com.wms.exception.BusinessException;
import com.wms.mapper.InventoryAlertMapper;
import com.wms.mapper.InventoryBatchMapper;
import com.wms.mapper.ProductMapper;
import com.wms.service.InventoryAlertService;
import com.wms.statemachine.InventoryState;
import com.wms.statemachine.InventoryStateMachine;
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
public class InventoryAlertServiceImpl implements InventoryAlertService {

    @Autowired
    private InventoryAlertMapper inventoryAlertMapper;

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryStateMachine inventoryStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkExpireAlert() {
        log.info("开始执行效期预警检查");

        InventoryQueryDTO query = new InventoryQueryDTO();
        query.setPageNum(1);
        query.setPageSize(Integer.MAX_VALUE);

        List<InventoryBatch> batches = inventoryBatchMapper.selectList(query);
        if (batches == null || batches.isEmpty()) {
            log.info("没有需要检查的库存批次");
            return;
        }

        Date now = new Date();
        int alertCount = 0;

        for (InventoryBatch batch : batches) {
            if (batch.getExpireDate() == null) {
                continue;
            }

            Product product = productMapper.selectById(batch.getProductId());
            if (product == null) {
                continue;
            }

            long remainingDays = (batch.getExpireDate().getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
            int warningDays = product.getWarningDays() != null ? product.getWarningDays() : 30;

            if (remainingDays <= 0 || remainingDays <= warningDays) {
                Integer alertType = remainingDays <= 0 ? 4 : 3;
                Integer alertLevel = remainingDays <= 0 ? 3 : (remainingDays <= 7 ? 3 : (remainingDays <= 15 ? 2 : 1));

                InventoryAlert existingAlert = findExistingAlert(batch, alertType);
                if (existingAlert != null && existingAlert.getStatus() != 4) {
                    continue;
                }

                InventoryAlert alert = new InventoryAlert();
                alert.setAlertNo(generateAlertNo());
                alert.setAlertType(alertType);
                alert.setWarehouseId(batch.getWarehouseId());
                alert.setLocationId(batch.getLocationId());
                alert.setProductId(batch.getProductId());
                alert.setBatchNo(batch.getBatchNo());
                alert.setCurrentQuantity(batch.getQuantity());
                alert.setThresholdQuantity(BigDecimal.ZERO);
                alert.setCurrentDate(now);
                alert.setExpireDate(batch.getExpireDate());
                alert.setRemainingDays((int) remainingDays);
                alert.setAlertLevel(alertLevel);
                alert.setAlertTime(now);
                alert.setStatus(1);
                alert.setRemark(remainingDays <= 0 ? "商品已过期" : "商品临期，剩余" + remainingDays + "天");
                alert.setCreateTime(new Date());
                alert.setUpdateTime(new Date());

                inventoryAlertMapper.insert(alert);
                alertCount++;

                if (remainingDays <= 0 && batch.getInventoryStatus() != 4) {
                    inventoryBatchMapper.updateStatus(batch.getId(), 4);
                } else if (remainingDays <= warningDays && remainingDays > 0 && batch.getInventoryStatus() == 1) {
                    inventoryBatchMapper.updateStatus(batch.getId(), 2);
                }
            }
        }

        log.info("效期预警检查完成，生成预警{}条", alertCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkStockThresholdAlert() {
        log.info("开始执行库存上下限预警检查");

        InventoryQueryDTO query = new InventoryQueryDTO();
        query.setPageNum(1);
        query.setPageSize(Integer.MAX_VALUE);

        List<InventoryBatch> batches = inventoryBatchMapper.selectList(query);
        if (batches == null || batches.isEmpty()) {
            log.info("没有需要检查的库存批次");
            return;
        }

        int alertCount = 0;

        for (InventoryBatch batch : batches) {
            Product product = productMapper.selectById(batch.getProductId());
            if (product == null) {
                continue;
            }

            BigDecimal totalQty = batch.getQuantity();

            if (product.getMinStock() != null && totalQty.compareTo(product.getMinStock()) < 0) {
                InventoryAlert existingAlert = findExistingAlert(batch, 1);
                if (existingAlert == null || existingAlert.getStatus() == 4) {
                    InventoryAlert alert = new InventoryAlert();
                    alert.setAlertNo(generateAlertNo());
                    alert.setAlertType(1);
                    alert.setWarehouseId(batch.getWarehouseId());
                    alert.setLocationId(batch.getLocationId());
                    alert.setProductId(batch.getProductId());
                    alert.setBatchNo(batch.getBatchNo());
                    alert.setCurrentQuantity(totalQty);
                    alert.setThresholdQuantity(product.getMinStock());
                    alert.setAlertLevel(totalQty.compareTo(BigDecimal.ZERO) == 0 ? 3 : 2);
                    alert.setAlertTime(new Date());
                    alert.setStatus(1);
                    alert.setRemark("库存不足，当前：" + totalQty + "，最低：" + product.getMinStock());
                    alert.setCreateTime(new Date());
                    alert.setUpdateTime(new Date());

                    inventoryAlertMapper.insert(alert);
                    alertCount++;
                }
            }

            if (product.getMaxStock() != null && totalQty.compareTo(product.getMaxStock()) > 0) {
                InventoryAlert existingAlert = findExistingAlert(batch, 2);
                if (existingAlert == null || existingAlert.getStatus() == 4) {
                    InventoryAlert alert = new InventoryAlert();
                    alert.setAlertNo(generateAlertNo());
                    alert.setAlertType(2);
                    alert.setWarehouseId(batch.getWarehouseId());
                    alert.setLocationId(batch.getLocationId());
                    alert.setProductId(batch.getProductId());
                    alert.setBatchNo(batch.getBatchNo());
                    alert.setCurrentQuantity(totalQty);
                    alert.setThresholdQuantity(product.getMaxStock());
                    alert.setAlertLevel(1);
                    alert.setAlertTime(new Date());
                    alert.setStatus(1);
                    alert.setRemark("库存过量，当前：" + totalQty + "，最高：" + product.getMaxStock());
                    alert.setCreateTime(new Date());
                    alert.setUpdateTime(new Date());

                    inventoryAlertMapper.insert(alert);
                    alertCount++;
                }
            }
        }

        log.info("库存上下限预警检查完成，生成预警{}条", alertCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlert(Long alertId, String handleResult, String operator) {
        InventoryAlert alert = inventoryAlertMapper.selectById(alertId);
        if (alert == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "预警记录不存在");
        }
        if (alert.getStatus() == 3 || alert.getStatus() == 4) {
            throw new BusinessException(ResultCode.BUSINESS_STATUS_ERROR, "该预警已处理或已忽略");
        }

        int rows = inventoryAlertMapper.updateStatus(alertId, 3, operator, handleResult);
        if (rows != 1) {
            throw new BusinessException(ResultCode.DATABASE_ERROR, "预警处理失败");
        }

        log.info("预警处理成功: alertId={}, operator={}", alertId, operator);
    }

    @Override
    public PageResult<InventoryAlert> queryAlerts(PageQuery query, Integer alertType,
                                                   Integer alertLevel, Integer status,
                                                   Long warehouseId, Long productId) {
        List<InventoryAlert> list = inventoryAlertMapper.selectList(query, alertType, alertLevel,
                status, warehouseId, productId);
        return PageResult.of(query.getPageNum(), query.getPageSize(), (long) list.size(), list);
    }

    @Override
    public InventoryAlert getById(Long id) {
        return inventoryAlertMapper.selectById(id);
    }

    @Override
    public int countPendingAlerts() {
        return inventoryAlertMapper.countPending();
    }

    private InventoryAlert findExistingAlert(InventoryBatch batch, Integer alertType) {
        PageQuery query = new PageQuery();
        query.setPageNum(1);
        query.setPageSize(1);

        List<InventoryAlert> alerts = inventoryAlertMapper.selectList(
                query, alertType, null, null,
                batch.getWarehouseId(), batch.getProductId());

        if (alerts != null && !alerts.isEmpty()) {
            for (InventoryAlert alert : alerts) {
                if (batch.getBatchNo().equals(alert.getBatchNo())
                        && batch.getLocationId().equals(alert.getLocationId())) {
                    return alert;
                }
            }
        }
        return null;
    }

    private String generateAlertNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return "AL" + sdf.format(new Date()) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

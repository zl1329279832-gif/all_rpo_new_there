package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.InventoryQueryDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.InventoryLog;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {

    PageResult<InventoryBatch> queryInventoryBatch(InventoryQueryDTO query);

    PageResult<InventoryLog> queryInventoryLog(PageQuery query, Long warehouseId,
                                               Long productId, String batchNo,
                                               Integer businessType, String businessNo);

    void stockIn(Long warehouseId, Long locationId, Long productId, String batchNo,
                 Long supplierId, BigDecimal quantity, String unit,
                 java.util.Date produceDate, java.util.Date expireDate,
                 BigDecimal costPrice, String businessNo, String operator, String remark);

    List<InventoryBatch> allocateForOutbound(Long warehouseId, Long productId,
                                              BigDecimal requiredQuantity,
                                              Integer outboundStrategy,
                                              String specifyBatchNo);

    void lockInventory(Long batchId, BigDecimal quantity, String businessNo, String operator);

    void unlockInventory(Long batchId, BigDecimal quantity, String businessNo, String operator);

    void reduceInventory(Long batchId, BigDecimal quantity, String businessNo, String operator);

    void reduceLockedInventory(Long batchId, BigDecimal quantity, String businessNo, String operator);

    void freezeInventory(Long batchId, BigDecimal quantity, String businessNo, String operator);

    void unfreezeInventory(Long batchId, BigDecimal quantity, String businessNo, String operator);

    void updateInventoryStatus(Long batchId, Integer targetStatus, String operator);

    List<InventoryLog> queryBatchTrace(String batchNo, Long warehouseId, Long productId);

    boolean checkLocationCapacity(Long locationId, BigDecimal quantity);

    InventoryBatch getById(Long id);
}

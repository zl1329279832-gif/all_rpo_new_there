package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.entity.InventoryAlert;

import java.util.List;

public interface InventoryAlertService {

    void checkExpireAlert();

    void checkStockThresholdAlert();

    void handleAlert(Long alertId, String handleResult, String operator);

    PageResult<InventoryAlert> queryAlerts(PageQuery query, Integer alertType,
                                            Integer alertLevel, Integer status,
                                            Long warehouseId, Long productId);

    InventoryAlert getById(Long id);

    int countPendingAlerts();
}

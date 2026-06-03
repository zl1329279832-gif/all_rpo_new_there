package com.wms.lock;

import cn.hutool.core.util.StrUtil;

public class LockKeyGenerator {

    private static final String INVENTORY_LOCK = "inventory:";
    private static final String LOCATION_LOCK = "location:";
    private static final String BATCH_LOCK = "batch:";
    private static final String RECEIPT_LOCK = "receipt:";
    private static final String SHIPMENT_LOCK = "shipment:";
    private static final String PICKING_LOCK = "picking:";
    private static final String STOCKTAKE_LOCK = "stocktake:";
    private static final String RETURN_LOCK = "return:";

    public static String generateInventoryLock(Long productId) {
        return INVENTORY_LOCK + productId;
    }

    public static String generateInventoryLock(Long warehouseId, Long productId) {
        return INVENTORY_LOCK + warehouseId + ":" + productId;
    }

    public static String generateLocationLock(Long locationId) {
        return LOCATION_LOCK + locationId;
    }

    public static String generateBatchLock(String batchNo, Long productId, Long locationId) {
        return BATCH_LOCK + batchNo + ":" + productId + ":" + locationId;
    }

    public static String generateReceiptLock(Long receiptId) {
        return RECEIPT_LOCK + receiptId;
    }

    public static String generateShipmentLock(Long shipmentId) {
        return SHIPMENT_LOCK + shipmentId;
    }

    public static String generatePickingLock(Long taskId) {
        return PICKING_LOCK + taskId;
    }

    public static String generateStocktakeLock(Long stocktakeId) {
        return STOCKTAKE_LOCK + stocktakeId;
    }

    public static String generateReturnLock(Long returnId) {
        return RETURN_LOCK + returnId;
    }

    public static String generateCustomLock(String... parts) {
        return StrUtil.join(":", (Object[]) parts);
    }
}

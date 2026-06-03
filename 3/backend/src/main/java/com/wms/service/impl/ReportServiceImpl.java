package com.wms.service.impl;

import com.wms.entity.InventoryBatch;
import com.wms.entity.InventoryLog;
import com.wms.entity.InventoryAlert;
import com.wms.entity.Warehouse;
import com.wms.mapper.InventoryBatchMapper;
import com.wms.mapper.InventoryLogMapper;
import com.wms.mapper.InventoryAlertMapper;
import com.wms.mapper.WarehouseMapper;
import com.wms.service.ReportService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private InventoryBatchMapper inventoryBatchMapper;
    @Autowired
    private InventoryLogMapper inventoryLogMapper;
    @Autowired
    private InventoryAlertMapper inventoryAlertMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalInventory = inventoryBatchMapper.selectTotalQuantity();
        result.put("totalInventory", totalInventory != null ? totalInventory : BigDecimal.ZERO);

        List<InventoryLog> inboundLogs = inventoryLogMapper.selectList(null, null, null, null, 1, null);
        BigDecimal inboundTotal = inboundLogs.stream()
                .map(InventoryLog::getChangeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("inboundTotal", inboundTotal);

        List<InventoryLog> outboundLogs = inventoryLogMapper.selectList(null, null, null, null, 2, null);
        BigDecimal outboundTotal = outboundLogs.stream()
                .map(InventoryLog::getChangeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("outboundTotal", outboundTotal);

        List<InventoryAlert> alerts = inventoryAlertMapper.selectList(null, null, null, 0, null, null);
        result.put("alertCount", alerts.size());

        long nearExpireCount = alerts.stream()
                .filter(a -> a.getAlertType() != null && a.getAlertType() == 2)
                .count();
        result.put("nearExpireCount", nearExpireCount);

        long lowStockCount = alerts.stream()
                .filter(a -> a.getAlertType() != null && a.getAlertType() == 3)
                .count();
        result.put("lowStockCount", lowStockCount);

        long overStockCount = alerts.stream()
                .filter(a -> a.getAlertType() != null && a.getAlertType() == 4)
                .count();
        result.put("overStockCount", overStockCount);

        return result;
    }

    @Override
    public List<Map<String, Object>> getTrend(String type, String startDate, String endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        Date start = startDate != null ? DateUtil.parse(startDate) : DateUtil.offsetDay(new Date(), -6);
        Date end = endDate != null ? DateUtil.parse(endDate) : new Date();

        List<Date> dates = new ArrayList<>();
        Date current = DateUtil.beginOfDay(start);
        Date endDay = DateUtil.beginOfDay(end);
        while (!current.after(endDay)) {
            dates.add(current);
            current = DateUtil.offsetDay(current, 1);
        }

        for (Date date : dates) {
            Map<String, Object> item = new HashMap<>();
            String dateStr = DateUtil.format(date, "yyyy-MM-dd");
            item.put("date", dateStr);

            Date dayStart = DateUtil.beginOfDay(date);
            Date dayEnd = DateUtil.endOfDay(date);

            List<InventoryLog> allLogs = inventoryLogMapper.selectList(null, null, null, null, null, null);
            List<InventoryLog> dayLogs = allLogs.stream()
                    .filter(log -> log.getCreateTime() != null 
                            && !log.getCreateTime().before(dayStart) 
                            && !log.getCreateTime().after(dayEnd))
                    .collect(Collectors.toList());

            BigDecimal inboundQty = dayLogs.stream()
                    .filter(log -> log.getBusinessType() != null && log.getBusinessType() == 1)
                    .map(InventoryLog::getChangeQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("inboundQuantity", inboundQty);

            BigDecimal outboundQty = dayLogs.stream()
                    .filter(log -> log.getBusinessType() != null && log.getBusinessType() == 2)
                    .map(InventoryLog::getChangeQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("outboundQuantity", outboundQty);

            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getWarehouseReport() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Warehouse> warehouses = warehouseMapper.getAll();

        List<InventoryBatch> allBatches = inventoryBatchMapper.selectList(null);
        List<InventoryAlert> allAlerts = inventoryAlertMapper.selectList(null, null, null, null, null, null);

        for (Warehouse warehouse : warehouses) {
            Map<String, Object> item = new HashMap<>();
            item.put("warehouseId", warehouse.getId());
            item.put("warehouseName", warehouse.getWarehouseName());

            List<InventoryBatch> batches = allBatches.stream()
                    .filter(b -> warehouse.getId().equals(b.getWarehouseId()))
                    .collect(Collectors.toList());
            BigDecimal totalQty = batches.stream()
                    .map(InventoryBatch::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("totalQuantity", totalQty);

            int skuCount = (int) batches.stream()
                    .map(InventoryBatch::getProductId)
                    .distinct()
                    .count();
            item.put("skuCount", skuCount);
            item.put("batchCount", batches.size());

            List<InventoryAlert> alerts = allAlerts.stream()
                    .filter(a -> warehouse.getId().equals(a.getWarehouseId()) && (a.getStatus() == null || a.getStatus() == 0))
                    .collect(Collectors.toList());
            item.put("alertCount", alerts.size());

            result.add(item);
        }

        return result;
    }

    @Override
    public byte[] exportReport(String type) {
        ExcelWriter writer = ExcelUtil.getWriter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            if ("inventory".equals(type)) {
                List<InventoryBatch> batches = inventoryBatchMapper.selectList(null);
                writer.addHeaderAlias("batchNo", "批次号");
                writer.addHeaderAlias("productId", "商品ID");
                writer.addHeaderAlias("quantity", "库存数量");
                writer.addHeaderAlias("availableQuantity", "可用数量");
                writer.addHeaderAlias("inboundDate", "入库日期");
                writer.addHeaderAlias("expireDate", "过期日期");
                writer.write(batches, true);
            } else if ("log".equals(type)) {
                List<InventoryLog> logs = inventoryLogMapper.selectList(null, null, null, null, null, null);
                writer.addHeaderAlias("businessNo", "业务单号");
                writer.addHeaderAlias("batchNo", "批次号");
                writer.addHeaderAlias("businessType", "业务类型");
                writer.addHeaderAlias("beforeQuantity", "操作前数量");
                writer.addHeaderAlias("changeQuantity", "变动数量");
                writer.addHeaderAlias("afterQuantity", "操作后数量");
                writer.addHeaderAlias("operator", "操作人");
                writer.addHeaderAlias("createTime", "操作时间");
                writer.write(logs, true);
            } else {
                Map<String, Object> overview = getOverview();
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(overview);
                writer.addHeaderAlias("totalInventory", "总库存");
                writer.addHeaderAlias("inboundTotal", "入库总量");
                writer.addHeaderAlias("outboundTotal", "出库总量");
                writer.addHeaderAlias("alertCount", "预警数量");
                writer.write(list, true);
            }

            writer.flush(out, true);
            writer.close();
            return out.toByteArray();
        } finally {
            IoUtil.close(writer);
            IoUtil.close(out);
        }
    }
}

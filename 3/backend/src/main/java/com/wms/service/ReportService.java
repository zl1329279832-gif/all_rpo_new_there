package com.wms.service;

import java.util.List;
import java.util.Map;

public interface ReportService {

    Map<String, Object> getOverview();

    List<Map<String, Object>> getTrend(String type, String startDate, String endDate);

    List<Map<String, Object>> getWarehouseReport();

    byte[] exportReport(String type);
}

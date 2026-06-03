package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@Api(tags = "统计报表接口")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @ApiOperation("报表概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> result = reportService.getOverview();
        return Result.success(result);
    }

    @ApiOperation("趋势报表")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(
            @ApiParam("类型") @RequestParam(required = false) String type,
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = reportService.getTrend(type, startDate, endDate);
        return Result.success(result);
    }

    @ApiOperation("仓库报表")
    @GetMapping("/warehouse")
    public Result<List<Map<String, Object>>> getWarehouseReport() {
        List<Map<String, Object>> result = reportService.getWarehouseReport();
        return Result.success(result);
    }

    @ApiOperation("导出报表")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @ApiParam("类型") @RequestParam(required = false, defaultValue = "overview") String type) {
        byte[] data = reportService.exportReport(type);
        String fileName;
        try {
            fileName = URLEncoder.encode("报表.xlsx", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fileName = "report.xlsx";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}

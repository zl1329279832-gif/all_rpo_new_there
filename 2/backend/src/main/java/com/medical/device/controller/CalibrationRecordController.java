package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.CalibrationRecord;
import com.medical.device.service.CalibrationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "校准记录", description = "校准记录管理接口")
@RestController
@RequestMapping("/calibration-records")
@RequiredArgsConstructor
public class CalibrationRecordController {

    private final CalibrationRecordService calibrationRecordService;

    @Operation(summary = "分页查询校准记录列表")
    @GetMapping
    public Result<PageResult<CalibrationRecord>> listRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "校准结果") @RequestParam(required = false) Integer calibrationResult,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PageResult<CalibrationRecord> result = calibrationRecordService.listRecords(pageNum, pageSize, keyword, calibrationResult, deviceId, startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取校准记录详情")
    @GetMapping("/{id}")
    public Result<CalibrationRecord> getRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        CalibrationRecord record = calibrationRecordService.getRecord(id);
        return Result.success(record);
    }

    @Operation(summary = "根据设备ID查询校准记录列表")
    @GetMapping("/device/{deviceId}")
    public Result<List<CalibrationRecord>> listByDeviceId(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        List<CalibrationRecord> records = calibrationRecordService.listByDeviceId(deviceId);
        return Result.success(records);
    }

    @Operation(summary = "创建校准记录")
    @PostMapping
    public Result<CalibrationRecord> createRecord(@RequestBody CalibrationRecord record) {
        CalibrationRecord created = calibrationRecordService.createRecord(record);
        return Result.success(created);
    }

    @Operation(summary = "更新校准记录")
    @PutMapping
    public Result<CalibrationRecord> updateRecord(@RequestBody CalibrationRecord record) {
        CalibrationRecord updated = calibrationRecordService.updateRecord(record);
        return Result.success(updated);
    }

    @Operation(summary = "删除校准记录")
    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        calibrationRecordService.deleteRecord(id);
        return Result.success("删除成功");
    }
}

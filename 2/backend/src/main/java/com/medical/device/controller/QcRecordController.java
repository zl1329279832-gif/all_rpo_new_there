package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.dto.QcRecordQueryDTO;
import com.medical.device.entity.QcRecord;
import com.medical.device.service.QcRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "质控记录", description = "质控记录管理接口")
@RestController
@RequestMapping("/qc-records")
@RequiredArgsConstructor
public class QcRecordController {

    private final QcRecordService qcRecordService;

    @Operation(summary = "分页查询质控记录列表")
    @GetMapping
    public Result<PageResult<QcRecord>> listRecords(@Valid @ModelAttribute QcRecordQueryDTO queryDTO) {
        PageResult<QcRecord> result = qcRecordService.listRecords(
                queryDTO.getPageNum(),
                queryDTO.getPageSize(),
                queryDTO.getKeyword(),
                queryDTO.getQcResult(),
                queryDTO.getQcType(),
                queryDTO.getDeviceId(),
                queryDTO.getStartDate(),
                queryDTO.getEndDate());
        return Result.success(result);
    }

    @Operation(summary = "获取质控记录详情")
    @GetMapping("/{id}")
    public Result<QcRecord> getRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        QcRecord record = qcRecordService.getRecord(id);
        return Result.success(record);
    }

    @Operation(summary = "根据设备ID查询质控记录列表")
    @GetMapping("/device/{deviceId}")
    public Result<List<QcRecord>> listByDeviceId(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        List<QcRecord> records = qcRecordService.listByDeviceId(deviceId);
        return Result.success(records);
    }

    @Operation(summary = "创建质控记录")
    @PostMapping
    public Result<QcRecord> createRecord(@RequestBody QcRecord record) {
        QcRecord created = qcRecordService.createRecord(record);
        return Result.success(created);
    }

    @Operation(summary = "更新质控记录")
    @PutMapping
    public Result<QcRecord> updateRecord(@RequestBody QcRecord record) {
        QcRecord updated = qcRecordService.updateRecord(record);
        return Result.success(updated);
    }

    @Operation(summary = "删除质控记录")
    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        qcRecordService.deleteRecord(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取质控统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = qcRecordService.getStatistics();
        return Result.success(stats);
    }
}

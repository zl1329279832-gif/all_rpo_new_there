package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.dto.DeviceQueryDTO;
import com.medical.device.entity.Device;
import com.medical.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备管理", description = "设备档案的增删改查及统计")
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "分页查询设备列表")
    @GetMapping
    public Result<PageResult<Device>> listDevices(@Valid @ModelAttribute DeviceQueryDTO queryDTO) {
        PageResult<Device> result = deviceService.listDevices(
                queryDTO.getPageNum(),
                queryDTO.getPageSize(),
                queryDTO.getKeyword(),
                queryDTO.getStatus(),
                queryDTO.getRiskLevel(),
                queryDTO.getDeptId());
        return Result.success(result);
    }

    @Operation(summary = "获取设备详情")
    @GetMapping("/{id}")
    public Result<Device> getDevice(@PathVariable Long id) {
        Device device = deviceService.getDevice(id);
        return Result.success(device);
    }

    @Operation(summary = "创建设备")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> createDevice(@RequestBody Device device) {
        deviceService.createDevice(device);
        return Result.success("创建设备成功");
    }

    @Operation(summary = "更新设备")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        device.setId(id);
        deviceService.updateDevice(device);
        return Result.success("更新设备成功");
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success("删除设备成功");
    }

    @Operation(summary = "更新设备质控状态")
    @PutMapping("/{id}/qc-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_STAFF')")
    public Result<String> updateQcStatus(@PathVariable Long id, @RequestParam Integer qcStatus) {
        deviceService.updateQcStatus(id, qcStatus);
        return Result.success("更新质控状态成功");
    }

    @Operation(summary = "获取高风险设备列表")
    @GetMapping("/high-risk")
    public Result<List<Device>> getHighRiskDevices() {
        List<Device> devices = deviceService.getHighRiskDevices();
        return Result.success(devices);
    }

    @Operation(summary = "获取设备统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDeviceStatistics() {
        Map<String, Object> stats = deviceService.getDeviceStatistics();
        return Result.success(stats);
    }
}

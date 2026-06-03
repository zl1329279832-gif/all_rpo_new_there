package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.MaintenanceContract;
import com.medical.device.service.MaintenanceContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "维保合同", description = "维保合同管理接口")
@RestController
@RequestMapping("/maintenance-contracts")
@RequiredArgsConstructor
public class MaintenanceContractController {

    private final MaintenanceContractService maintenanceContractService;

    @Operation(summary = "分页查询维保合同列表")
    @GetMapping
    public Result<PageResult<MaintenanceContract>> listContracts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "关键词（合同编号/名称/供应商）") @RequestParam(required = false) String keyword,
            @Parameter(description = "合同状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "合同类型") @RequestParam(required = false) Integer contractType,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId) {
        PageResult<MaintenanceContract> result = maintenanceContractService.listContracts(
                pageNum, pageSize, keyword, status, contractType, deviceId);
        return Result.success(result);
    }

    @Operation(summary = "获取维保合同详情")
    @GetMapping("/{id}")
    public Result<MaintenanceContract> getContract(@Parameter(description = "合同ID") @PathVariable Long id) {
        MaintenanceContract contract = maintenanceContractService.getContract(id);
        return Result.success(contract);
    }

    @Operation(summary = "新增维保合同")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<MaintenanceContract> createContract(@RequestBody MaintenanceContract contract) {
        MaintenanceContract created = maintenanceContractService.createContract(contract);
        return Result.success(created);
    }

    @Operation(summary = "修改维保合同")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<MaintenanceContract> updateContract(@RequestBody MaintenanceContract contract) {
        MaintenanceContract updated = maintenanceContractService.updateContract(contract);
        return Result.success(updated);
    }

    @Operation(summary = "删除维保合同")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> deleteContract(@Parameter(description = "合同ID") @PathVariable Long id) {
        maintenanceContractService.deleteContract(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "变更合同状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> updateContractStatus(
            @Parameter(description = "合同ID") @PathVariable Long id,
            @Parameter(description = "目标状态") @RequestParam Integer status) {
        maintenanceContractService.updateContractStatus(id, status);
        return Result.success("状态变更成功");
    }

    @Operation(summary = "查询即将到期的合同")
    @GetMapping("/expiring")
    public Result<List<MaintenanceContract>> getExpiringContracts(
            @Parameter(description = "提前提醒天数") @RequestParam(defaultValue = "30") int days) {
        List<MaintenanceContract> contracts = maintenanceContractService.getExpiringContracts(days);
        return Result.success(contracts);
    }
}

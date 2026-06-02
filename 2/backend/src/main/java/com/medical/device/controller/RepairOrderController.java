package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.PartReplacement;
import com.medical.device.entity.RepairOrder;
import com.medical.device.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "维修工单", description = "维修工单管理流程")
@RestController
@RequestMapping("/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @Operation(summary = "分页查询工单列表")
    @GetMapping
    public Result<PageResult<RepairOrder>> listOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer faultLevel,
            @RequestParam(required = false) Long deviceId) {
        PageResult<RepairOrder> result = repairOrderService.listOrders(pageNum, pageSize, keyword, status, faultLevel, deviceId);
        return Result.success(result);
    }

    @Operation(summary = "获取工单详情")
    @GetMapping("/{id}")
    public Result<RepairOrder> getOrder(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getOrder(id);
        return Result.success(order);
    }

    @Operation(summary = "创建维修工单")
    @PostMapping
    public Result<RepairOrder> createOrder(@RequestBody RepairOrder order) {
        RepairOrder created = repairOrderService.createOrder(order);
        return Result.success(created);
    }

    @Operation(summary = "派单")
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> assignOrder(@PathVariable Long id,
                                    @RequestParam Long repairerId,
                                    @RequestParam String repairerName) {
        repairOrderService.assignOrder(id, repairerId, repairerName);
        return Result.success("派单成功");
    }

    @Operation(summary = "开始维修")
    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public Result<String> startRepair(@PathVariable Long id) {
        repairOrderService.startRepair(id);
        return Result.success("开始维修");
    }

    @Operation(summary = "完成维修")
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public Result<String> completeRepair(@PathVariable Long id,
                                       @RequestParam String repairContent,
                                       @RequestParam String repairResult,
                                       @RequestBody(required = false) List<PartReplacement> parts) {
        repairOrderService.completeRepair(id, repairContent, repairResult, parts);
        return Result.success("维修完成");
    }

    @Operation(summary = "验收工单")
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> acceptOrder(@PathVariable Long id, @RequestParam(required = false) Integer qcStatus) {
        repairOrderService.acceptOrder(id, qcStatus);
        return Result.success("验收通过");
    }

    @Operation(summary = "获取维修统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = repairOrderService.getStatistics();
        return Result.success(stats);
    }
}

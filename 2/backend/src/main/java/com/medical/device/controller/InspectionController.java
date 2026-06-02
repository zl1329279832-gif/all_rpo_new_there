package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.InspectionPlan;
import com.medical.device.entity.InspectionTask;
import com.medical.device.service.InspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "巡检管理", description = "巡检计划和任务管理")
@RestController
@RequestMapping("/inspection")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @Operation(summary = "分页查询巡检计划")
    @GetMapping("/plans")
    public Result<PageResult<InspectionPlan>> listPlans(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<InspectionPlan> result = inspectionService.listPlans(pageNum, pageSize, keyword, status);
        return Result.success(result);
    }

    @Operation(summary = "创建巡检计划")
    @PostMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<Void> createPlan(@RequestBody InspectionPlan plan) {
        inspectionService.createPlan(plan);
        return Result.success("计划创建成功");
    }

    @Operation(summary = "更新巡检计划")
    @PutMapping("/plans/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<Void> updatePlan(@PathVariable Long id, @RequestBody InspectionPlan plan) {
        plan.setId(id);
        inspectionService.updatePlan(plan);
        return Result.success("计划更新成功");
    }

    @Operation(summary = "分页查询巡检任务")
    @GetMapping("/tasks")
    public Result<PageResult<InspectionTask>> listTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        PageResult<InspectionTask> result = inspectionService.listTasks(pageNum, pageSize, keyword, status, deviceId, startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取日历视图任务")
    @GetMapping("/tasks/calendar")
    public Result<List<InspectionTask>> getCalendarTasks(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<InspectionTask> tasks = inspectionService.getTasksByDateRange(startDate, endDate);
        return Result.success(tasks);
    }

    @Operation(summary = "执行巡检任务")
    @PutMapping("/tasks/{id}/execute")
    public Result<Void> executeTask(@PathVariable Long id,
                                    @RequestParam Integer checkResult,
                                    @RequestParam(required = false) String abnormalDesc,
                                    @RequestParam(required = false) String handleSuggestion,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        inspectionService.executeTask(id, checkResult, abnormalDesc, handleSuggestion, userId, username);
        return Result.success("任务执行完成");
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/tasks/{id}")
    public Result<InspectionTask> getTaskDetail(@PathVariable Long id) {
        InspectionTask task = inspectionService.getTaskDetail(id);
        return Result.success(task);
    }

    @Operation(summary = "获取巡检统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = inspectionService.getStatistics();
        return Result.success(stats);
    }
}

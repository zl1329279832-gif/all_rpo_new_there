package com.medical.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.device.common.Result;
import com.medical.device.entity.CalibrationRecord;
import com.medical.device.mapper.CalibrationRecordMapper;
import com.medical.device.mapper.DeviceMapper;
import com.medical.device.mapper.InspectionTaskMapper;
import com.medical.device.mapper.RepairOrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "统计管理", description = "综合统计数据查询接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final DeviceMapper deviceMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final InspectionTaskMapper inspectionTaskMapper;
    private final CalibrationRecordMapper calibrationRecordMapper;

    @Operation(summary = "获取首页概览统计", description = "获取设备总数、待维修数、运行中数、待校准数、高风险设备数等首页关键指标")
    @GetMapping("/overview")
    @Cacheable(cacheNames = "statistics", key = "'overview'")
    public Result<Map<String, Object>> getOverviewStatistics() {
        Map<String, Object> overview = new HashMap<>();

        Long totalDevices = deviceMapper.selectCount(null);
        overview.put("totalDevices", totalDevices);

        List<Map<String, Object>> statusStats = deviceMapper.countByStatus();
        long runningCount = 0;
        long pendingRepairCount = 0;
        for (Map<String, Object> stat : statusStats) {
            Integer status = (Integer) stat.get("status");
            Long count = ((Number) stat.get("count")).longValue();
            if (status != null) {
                switch (status) {
                    case 1:
                        runningCount = count;
                        break;
                    case 2:
                        pendingRepairCount = count;
                        break;
                }
            }
        }
        overview.put("runningCount", runningCount);
        overview.put("pendingRepairCount", pendingRepairCount);

        Long highRiskCount = deviceMapper.countHighRiskDevices();
        overview.put("highRiskCount", highRiskCount);

        Long pendingCalibrationCount = calibrationRecordMapper.selectCount(
            new LambdaQueryWrapper<CalibrationRecord>()
                .lt(CalibrationRecord::getNextCalibrationDate, LocalDate.now().plusDays(7))
                .eq(CalibrationRecord::getCalibrationResult, 1)
        );
        overview.put("pendingCalibrationCount", pendingCalibrationCount);

        List<Map<String, Object>> repairStatusStats = repairOrderMapper.countByStatus();
        long pendingRepairOrders = 0;
        for (Map<String, Object> stat : repairStatusStats) {
            Integer status = (Integer) stat.get("status");
            Long count = ((Number) stat.get("count")).longValue();
            if (status != null && status < 5) {
                pendingRepairOrders += count;
            }
        }
        overview.put("pendingRepairOrders", pendingRepairOrders);

        Integer totalDowntime = repairOrderMapper.sumTotalDowntime();
        overview.put("totalDowntime", totalDowntime != null ? totalDowntime : 0);

        return Result.success(overview);
    }

    @Operation(summary = "获取仪表盘所有统计数据", description = "获取设备状态分布、维修趋势、近7天巡检数据等仪表盘完整数据")
    @GetMapping("/dashboard")
    @Cacheable(cacheNames = "statistics", key = "'dashboard'")
    public Result<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("statusDistribution", deviceMapper.countByStatus());

        dashboard.put("riskLevelDistribution", deviceMapper.countByRiskLevel());

        dashboard.put("deptDistribution", deviceMapper.countByDept());

        dashboard.put("repairTrend", repairOrderMapper.countMonthlyTrend());

        dashboard.put("last7DaysInspection", inspectionTaskMapper.countLast7Days());

        dashboard.put("inspectionStatusStats", inspectionTaskMapper.countByStatus());

        dashboard.put("repairStatusStats", repairOrderMapper.countByStatus());

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalDevices", deviceMapper.selectCount(null));
        overview.put("highRiskCount", deviceMapper.countHighRiskDevices());
        Integer totalDowntime = repairOrderMapper.sumTotalDowntime();
        overview.put("totalDowntime", totalDowntime != null ? totalDowntime : 0);
        dashboard.put("overview", overview);

        return Result.success(dashboard);
    }

    @Operation(summary = "获取月度数据汇总", description = "按月度汇总设备、维修、巡检、校准等各项数据")
    @GetMapping("/monthly")
    @Cacheable(cacheNames = "statistics", key = "'monthly'")
    public Result<Map<String, Object>> getMonthlyStatistics() {
        Map<String, Object> monthly = new HashMap<>();

        monthly.put("deviceStatus", deviceMapper.countByStatus());

        monthly.put("repairTrend", repairOrderMapper.countMonthlyTrend());

        monthly.put("repairStatus", repairOrderMapper.countByStatus());

        monthly.put("inspectionStatus", inspectionTaskMapper.countByStatus());

        monthly.put("last7DaysInspection", inspectionTaskMapper.countLast7Days());

        monthly.put("highRiskDevices", deviceMapper.selectHighRiskDevices());

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDevices", deviceMapper.selectCount(null));
        summary.put("highRiskCount", deviceMapper.countHighRiskDevices());
        summary.put("totalRepairOrders", repairOrderMapper.selectCount(null));
        summary.put("totalInspectionTasks", inspectionTaskMapper.selectCount(null));
        summary.put("totalCalibrationRecords", calibrationRecordMapper.selectCount(null));
        Integer totalDowntime = repairOrderMapper.sumTotalDowntime();
        summary.put("totalDowntime", totalDowntime != null ? totalDowntime : 0);
        monthly.put("summary", summary);

        return Result.success(monthly);
    }
}

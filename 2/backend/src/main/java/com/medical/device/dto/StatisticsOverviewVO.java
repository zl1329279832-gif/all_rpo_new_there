package com.medical.device.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 首页概览统计VO
 */
@Data
public class StatisticsOverviewVO {

    private Long totalDevices;

    private Long runningCount;

    private Long repairCount;

    private Long calibrationCount;

    private Long highRiskCount;

    private Long totalInspection;

    private Long completedInspection;

    private BigDecimal passRate;
}

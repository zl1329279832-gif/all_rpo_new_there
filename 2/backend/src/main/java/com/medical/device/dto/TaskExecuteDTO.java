package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 巡检任务执行DTO
 */
@Data
public class TaskExecuteDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "检查结果不能为空")
    private Integer checkResult;

    private String abnormalDescription;

    private String handleSuggestion;

    private LocalDateTime actualDate;

    private Long inspectorId;

    private String inspectorName;
}

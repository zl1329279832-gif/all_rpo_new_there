package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 维修完成DTO
 */
@Data
public class RepairCompleteDTO {

    @NotNull(message = "工单ID不能为空")
    private Long repairOrderId;

    @NotBlank(message = "维修内容不能为空")
    private String repairContent;

    @NotBlank(message = "维修结果不能为空")
    private String repairResult;

    private Integer downtime;

    private BigDecimal repairCost;

    private LocalDateTime completeTime;

    private Long repairerId;

    private String repairerName;

    @Valid
    private List<PartReplacementDTO> partReplacements;

    private String remark;
}

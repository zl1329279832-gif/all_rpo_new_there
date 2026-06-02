package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 维修工单创建DTO
 */
@Data
public class RepairOrderCreateDTO {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotBlank(message = "故障描述不能为空")
    private String faultDesc;

    @NotNull(message = "故障类型不能为空")
    private Integer faultType;

    @NotNull(message = "故障等级不能为空")
    private Integer faultLevel;

    private String faultImages;

    private String reporterName;

    private String reporterPhone;

    private Long reporterId;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private String remark;
}

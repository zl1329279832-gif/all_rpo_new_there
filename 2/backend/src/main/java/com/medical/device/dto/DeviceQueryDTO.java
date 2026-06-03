package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 设备查询条件DTO
 */
@Data
public class DeviceQueryDTO {

    private String keyword;

    private Integer status;

    private Integer riskLevel;

    private Long deptId;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;
}

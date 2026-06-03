package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 维修工单查询条件DTO
 */
@Data
public class RepairOrderQueryDTO {

    private String keyword;

    private Integer status;

    private Integer faultLevel;

    private Long deviceId;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数不能小于1")
    private Integer pageSize = 10;
}

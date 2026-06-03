package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("预警处理DTO")
public class AlertHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预警ID", required = true, example = "1")
    @NotNull(message = "预警ID不能为空")
    private Long alertId;

    @ApiModelProperty(value = "处理结果", required = true)
    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
}

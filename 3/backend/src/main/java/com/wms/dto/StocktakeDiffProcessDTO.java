package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("盘点差异处理DTO")
public class StocktakeDiffProcessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "盘点单ID", required = true, example = "1")
    @NotNull(message = "盘点单ID不能为空")
    private Long stocktakeId;

    @ApiModelProperty(value = "明细ID", required = true, example = "1")
    @NotNull(message = "明细ID不能为空")
    private Long detailId;

    @ApiModelProperty(value = "处理状态：1-已调整 2-忽略差异", required = true, example = "1")
    @NotNull(message = "处理状态不能为空")
    private Integer processStatus;

    @ApiModelProperty(value = "处理结果", required = true)
    @NotBlank(message = "处理结果不能为空")
    private String processResult;
}

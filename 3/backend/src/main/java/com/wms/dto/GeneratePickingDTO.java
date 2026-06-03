package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("生成拣货任务DTO")
public class GeneratePickingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "出库单ID列表", required = true)
    @NotEmpty(message = "出库单ID列表不能为空")
    private List<Long> shipmentOrderIds;

    @ApiModelProperty(value = "拣货模式：1-按单拣货 2-波次拣货", required = true, example = "1")
    @NotNull(message = "拣货模式不能为空")
    private Integer pickingMode;

    @ApiModelProperty(value = "拣货员")
    private String picker;

    @ApiModelProperty(value = "优先级：1-高 2-中 3-低", example = "2")
    private Integer priority;
}

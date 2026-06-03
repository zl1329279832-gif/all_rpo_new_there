package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("出库撤销DTO")
public class ShipmentCancelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "出库单ID", required = true, example = "1")
    @NotNull(message = "出库单ID不能为空")
    private Long shipmentOrderId;

    @ApiModelProperty(value = "撤销原因", required = true)
    @NotBlank(message = "撤销原因不能为空")
    private String cancelReason;
}

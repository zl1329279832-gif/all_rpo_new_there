package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("退货入库确认DTO")
public class ReturnConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "退货单ID", required = true, example = "1")
    @NotNull(message = "退货单ID不能为空")
    private Long returnOrderId;

    @ApiModelProperty(value = "明细ID", required = true, example = "1")
    @NotNull(message = "明细ID不能为空")
    private Long detailId;

    @ApiModelProperty(value = "入库库位ID", required = true, example = "1")
    @NotNull(message = "入库库位ID不能为空")
    private Long locationId;
}

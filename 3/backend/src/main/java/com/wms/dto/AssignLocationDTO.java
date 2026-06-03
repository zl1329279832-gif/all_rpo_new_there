package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("库位分配DTO")
public class AssignLocationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "入库单ID", required = true, example = "1")
    @NotNull(message = "入库单ID不能为空")
    private Long receiptOrderId;

    @ApiModelProperty(value = "明细ID", required = true, example = "1")
    @NotNull(message = "明细ID不能为空")
    private Long detailId;

    @ApiModelProperty(value = "库位ID", required = true, example = "1")
    @NotNull(message = "库位ID不能为空")
    private Long locationId;
}

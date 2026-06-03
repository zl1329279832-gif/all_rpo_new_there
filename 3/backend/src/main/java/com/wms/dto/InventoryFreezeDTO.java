package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("库存冻结DTO")
public class InventoryFreezeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "批次ID", required = true, example = "1")
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    @ApiModelProperty(value = "冻结数量", required = true, example = "10")
    @NotNull(message = "冻结数量不能为空")
    private BigDecimal quantity;

    @ApiModelProperty(value = "业务单据号")
    private String businessNo;

    @ApiModelProperty(value = "冻结原因")
    private String remark;
}

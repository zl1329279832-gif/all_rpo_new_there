package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("退货质检DTO")
public class ReturnInspectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "退货单ID", required = true, example = "1")
    @NotNull(message = "退货单ID不能为空")
    private Long returnOrderId;

    @ApiModelProperty(value = "明细ID", required = true, example = "1")
    @NotNull(message = "明细ID不能为空")
    private Long detailId;

    @ApiModelProperty(value = "质检结果：1-合格 2-不合格 3-待检", required = true, example = "1")
    @NotNull(message = "质检结果不能为空")
    private Integer inspectionResult;

    @ApiModelProperty(value = "实际数量", required = true, example = "10")
    @NotNull(message = "实际数量不能为空")
    private BigDecimal actualQuantity;

    @ApiModelProperty(value = "备注")
    private String remark;
}

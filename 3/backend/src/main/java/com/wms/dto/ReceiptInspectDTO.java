package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("入库质检DTO")
public class ReceiptInspectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "入库单ID", required = true, example = "1")
    @NotNull(message = "入库单ID不能为空")
    private Long receiptOrderId;

    @ApiModelProperty(value = "明细ID", required = true, example = "1")
    @NotNull(message = "明细ID不能为空")
    private Long detailId;

    @ApiModelProperty(value = "到货数量", required = true, example = "100")
    @NotNull(message = "到货数量不能为空")
    private BigDecimal arrivalQuantity;

    @ApiModelProperty(value = "合格数量", required = true, example = "95")
    @NotNull(message = "合格数量不能为空")
    private BigDecimal qualifiedQuantity;

    @ApiModelProperty(value = "不合格数量", required = true, example = "5")
    @NotNull(message = "不合格数量不能为空")
    private BigDecimal unqualifiedQuantity;

    @ApiModelProperty(value = "质检结果：1-合格 2-不合格 3-让步接收", required = true, example = "1")
    @NotNull(message = "质检结果不能为空")
    private Integer inspectionResult;

    @ApiModelProperty(value = "质检备注")
    private String inspectionRemark;
}

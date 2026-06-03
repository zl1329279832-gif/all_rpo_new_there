package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("到货确认DTO")
public class ReceiptArrivalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "入库单ID", required = true, example = "1")
    @NotNull(message = "入库单ID不能为空")
    private Long receiptOrderId;

    @ApiModelProperty(value = "到货时间")
    private Date arrivalTime;

    @ApiModelProperty(value = "备注")
    private String remark;
}

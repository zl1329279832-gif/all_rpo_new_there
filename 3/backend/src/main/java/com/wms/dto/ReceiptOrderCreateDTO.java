package com.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("入库单创建DTO")
public class ReceiptOrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "入库类型", required = true, example = "1")
    @NotNull(message = "入库类型不能为空")
    private Integer receiptType;

    @ApiModelProperty(value = "仓库ID", required = true, example = "1")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @ApiModelProperty(value = "供应商ID", example = "1")
    private Long supplierId;

    @ApiModelProperty(value = "来源单号")
    private String sourceOrderNo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "入库明细", required = true)
    @NotEmpty(message = "入库明细不能为空")
    @Valid
    private List<ReceiptDetailDTO> details;

    @Data
    @ApiModel("入库明细DTO")
    public static class ReceiptDetailDTO implements Serializable {
        @ApiModelProperty(value = "商品ID", required = true, example = "1")
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @ApiModelProperty(value = "批次号", required = true)
        @NotNull(message = "批次号不能为空")
        private String batchNo;

        @ApiModelProperty(value = "计划数量", required = true, example = "100")
        @NotNull(message = "计划数量不能为空")
        private BigDecimal planQuantity;

        @ApiModelProperty(value = "单位", required = true, example = "台")
        @NotNull(message = "单位不能为空")
        private String unit;

        @ApiModelProperty(value = "生产日期")
        private java.util.Date produceDate;

        @ApiModelProperty(value = "过期日期")
        private java.util.Date expireDate;

        @ApiModelProperty(value = "成本价", example = "100.00")
        private BigDecimal costPrice;

        @ApiModelProperty(value = "备注")
        private String remark;
    }
}

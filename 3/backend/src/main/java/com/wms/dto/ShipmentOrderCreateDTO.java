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
@ApiModel("出库单创建DTO")
public class ShipmentOrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "出库类型", required = true, example = "1")
    @NotNull(message = "出库类型不能为空")
    private Integer shipmentType;

    @ApiModelProperty(value = "仓库ID", required = true, example = "1")
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "来源单号")
    private String sourceOrderNo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "出库明细", required = true)
    @NotEmpty(message = "出库明细不能为空")
    @Valid
    private List<ShipmentDetailDTO> details;

    @Data
    @ApiModel("出库明细DTO")
    public static class ShipmentDetailDTO implements Serializable {
        @ApiModelProperty(value = "商品ID", required = true, example = "1")
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @ApiModelProperty(value = "计划数量", required = true, example = "10")
        @NotNull(message = "计划数量不能为空")
        private BigDecimal planQuantity;

        @ApiModelProperty(value = "单位", required = true, example = "台")
        @NotNull(message = "单位不能为空")
        private String unit;

        @ApiModelProperty(value = "出库策略：1-先进先出 2-效期优先 3-指定批次", example = "1")
        private Integer outboundStrategy = 1;

        @ApiModelProperty(value = "指定批次号（出库策略为3时必填）")
        private String specifyBatchNo;

        @ApiModelProperty(value = "单价", example = "199.00")
        private BigDecimal price;

        @ApiModelProperty(value = "备注")
        private String remark;
    }
}

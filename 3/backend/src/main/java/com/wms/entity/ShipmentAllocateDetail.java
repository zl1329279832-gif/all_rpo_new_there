package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("出库分配明细")
public class ShipmentAllocateDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("出库单ID")
    private Long shipmentOrderId;

    @ApiModelProperty("出库单明细ID")
    private Long shipmentDetailId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("分配数量")
    private BigDecimal allocateQuantity;

    @ApiModelProperty("已拣数量")
    private BigDecimal pickedQuantity;

    @ApiModelProperty("已复核数量")
    private BigDecimal reviewedQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("生产日期")
    private Date produceDate;

    @ApiModelProperty("过期日期")
    private Date expireDate;

    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

    @ApiModelProperty("是否已拣货：0-否 1-是")
    private Integer isPicked;

    @ApiModelProperty("是否已复核：0-否 1-是")
    private Integer isReviewed;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty(hidden = true)
    private String productCode;
    @ApiModelProperty(hidden = true)
    private String productName;
    @ApiModelProperty(hidden = true)
    private String locationCode;
}

package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("出库单明细")
public class ShipmentOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("出库单ID")
    private Long shipmentOrderId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("计划数量")
    private BigDecimal planQuantity;

    @ApiModelProperty("已分配数量")
    private BigDecimal allocatedQuantity;

    @ApiModelProperty("已拣数量")
    private BigDecimal pickedQuantity;

    @ApiModelProperty("已复核数量")
    private BigDecimal reviewedQuantity;

    @ApiModelProperty("实际出库数量")
    private BigDecimal actualQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("出库策略：1-先进先出(FIFO) 2-效期优先(FEFO) 3-指定批次")
    private Integer outboundStrategy;

    @ApiModelProperty("单价")
    private BigDecimal price;

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
    private String specification;
    @ApiModelProperty(hidden = true)
    private String outboundStrategyName;
    @ApiModelProperty(hidden = true)
    private List<ShipmentAllocateDetail> allocateDetails;
}

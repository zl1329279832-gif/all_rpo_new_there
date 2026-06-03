package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("盘点单明细")
public class StocktakeOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("盘点单ID")
    private Long stocktakeOrderId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("系统数量")
    private BigDecimal systemQuantity;

    @ApiModelProperty("初盘数量")
    private BigDecimal firstCount;

    @ApiModelProperty("复盘数量")
    private BigDecimal secondCount;

    @ApiModelProperty("最终数量")
    private BigDecimal finalCount;

    @ApiModelProperty("差异数量")
    private BigDecimal diffQuantity;

    @ApiModelProperty("差异类型：1-盘盈 2-盘亏 3-无差异")
    private Integer diffType;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("是否已盘点：0-否 1-是")
    private Integer isCounted;

    @ApiModelProperty("盘点时间")
    private Date countTime;

    @ApiModelProperty("盘点人")
    private String counter;

    @ApiModelProperty("差异原因")
    private String diffReason;

    @ApiModelProperty("处理状态：0-未处理 1-处理中 2-已处理")
    private Integer processStatus;

    @ApiModelProperty("处理结果")
    private String processResult;

    @ApiModelProperty("处理时间")
    private Date processTime;

    @ApiModelProperty("处理人")
    private String processor;

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
    @ApiModelProperty(hidden = true)
    private String diffTypeName;
}

package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("退货入库单明细")
public class ReturnOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("退货单ID")
    private Long returnOrderId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("退回批次号")
    private String batchNo;

    @ApiModelProperty("原出库批次号")
    private String originalBatchNo;

    @ApiModelProperty("退货数量")
    private BigDecimal returnQuantity;

    @ApiModelProperty("实际入库数量")
    private BigDecimal actualQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("质检结果：1-合格 2-不合格 3-待检")
    private Integer inspectionResult;

    @ApiModelProperty("入库库位ID")
    private Long locationId;

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

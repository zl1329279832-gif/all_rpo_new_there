package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("库存流水")
public class InventoryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("流水单号")
    private String logNo;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("业务类型：1-入库 2-出库 3-调拨 4-盘点 5-冻结 6-解冻 7-退货 8-调整")
    private Integer businessType;

    @ApiModelProperty("业务单据号")
    private String businessNo;

    @ApiModelProperty("变更前数量")
    private BigDecimal beforeQuantity;

    @ApiModelProperty("变更数量")
    private BigDecimal changeQuantity;

    @ApiModelProperty("变更后数量")
    private BigDecimal afterQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("操作类型：1-增加 2-减少")
    private Integer operationType;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("操作时间")
    private Date operationTime;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty(hidden = true)
    private String productCode;
    @ApiModelProperty(hidden = true)
    private String productName;
    @ApiModelProperty(hidden = true)
    private String locationCode;
    @ApiModelProperty(hidden = true)
    private String businessTypeName;
    @ApiModelProperty(hidden = true)
    private String operationTypeName;
}

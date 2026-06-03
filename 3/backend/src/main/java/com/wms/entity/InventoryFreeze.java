package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("库存冻结")
public class InventoryFreeze implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("冻结单号")
    private String freezeNo;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("冻结类型：1-盘点冻结 2-质检冻结 3-异常冻结 4-其他")
    private Integer freezeType;

    @ApiModelProperty("冻结原因")
    private String freezeReason;

    @ApiModelProperty("冻结数量")
    private BigDecimal freezeQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("冻结时间")
    private Date freezeTime;

    @ApiModelProperty("冻结操作人")
    private String freezeOperator;

    @ApiModelProperty("解冻时间")
    private Date unfreezeTime;

    @ApiModelProperty("解冻操作人")
    private String unfreezeOperator;

    @ApiModelProperty("解冻原因")
    private String unfreezeReason;

    @ApiModelProperty("状态：1-已冻结 2-已解冻")
    private Integer status;

    @ApiModelProperty("关联业务单号")
    private String businessNo;

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
    private String batchNoDisplay;
    @ApiModelProperty(hidden = true)
    private String locationCode;
    @ApiModelProperty(hidden = true)
    private String freezeTypeName;
    @ApiModelProperty(hidden = true)
    private String statusName;
}

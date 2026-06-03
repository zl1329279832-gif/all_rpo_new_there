package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("退货入库单")
public class ReturnOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("退货单号")
    private String returnNo;

    @ApiModelProperty("退货类型：1-销售退货 2-调拨退货 3-质量退货")
    private Integer returnType;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("原出库单号")
    private String originalShipmentNo;

    @ApiModelProperty("状态：1-待入库 2-质检中 3-待上架 4-已完成 5-已取消")
    private Integer status;

    @ApiModelProperty("退货总数量")
    private BigDecimal totalQuantity;

    @ApiModelProperty("实际入库数量")
    private BigDecimal actualQuantity;

    @ApiModelProperty("退货原因")
    private String returnReason;

    @ApiModelProperty("收货时间")
    private Date receiveTime;

    @ApiModelProperty("完成时间")
    private Date completeTime;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty(hidden = true)
    private String warehouseName;
    @ApiModelProperty(hidden = true)
    private String statusName;
    @ApiModelProperty(hidden = true)
    private List<ReturnOrderDetail> details;
}

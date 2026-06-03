package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("出库单")
public class ShipmentOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("出库单号")
    private String shipmentNo;

    @ApiModelProperty("出库类型：1-销售出库 2-调拨出库 3-退货出库 4-盘亏出库 5-报废出库")
    private Integer shipmentType;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("单据状态：1-待确认 2-已确认 3-拣货中 4-拣货完成 5-复核中 6-复核完成 7-已出库 8-已取消")
    private Integer orderStatus;

    @ApiModelProperty("总数量")
    private BigDecimal totalQuantity;

    @ApiModelProperty("已拣数量")
    private BigDecimal pickedQuantity;

    @ApiModelProperty("已复核数量")
    private BigDecimal reviewedQuantity;

    @ApiModelProperty("实际出库数量")
    private BigDecimal actualQuantity;

    @ApiModelProperty("分配时间")
    private Date allocateTime;

    @ApiModelProperty("拣货时间")
    private Date pickingTime;

    @ApiModelProperty("复核时间")
    private Date reviewTime;

    @ApiModelProperty("出库时间")
    private Date shipmentTime;

    @ApiModelProperty("取消时间")
    private Date cancelTime;

    @ApiModelProperty("取消原因")
    private String cancelReason;

    @ApiModelProperty("来源单号")
    private String sourceOrderNo;

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
    private String shipmentTypeName;
    @ApiModelProperty(hidden = true)
    private String orderStatusName;
    @ApiModelProperty(hidden = true)
    private List<ShipmentOrderDetail> details;
}

package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("拣货任务")
public class PickingTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("拣货任务号")
    private String taskNo;

    @ApiModelProperty("出库单ID")
    private Long shipmentOrderId;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("拣货员")
    private String picker;

    @ApiModelProperty("任务类型：1-订单拣货 2-补货拣货 3-调拨拣货")
    private Integer taskType;

    @ApiModelProperty("拣货模式：1-按单拣货 2-批量拣货 3-波次拣货")
    private Integer pickingMode;

    @ApiModelProperty("商品行数")
    private Integer totalItems;

    @ApiModelProperty("总数量")
    private BigDecimal totalQuantity;

    @ApiModelProperty("已拣行数")
    private Integer pickedItems;

    @ApiModelProperty("已拣数量")
    private BigDecimal pickedQuantity;

    @ApiModelProperty("状态：1-待拣货 2-拣货中 3-已完成 4-已取消")
    private Integer status;

    @ApiModelProperty("优先级：1-最高 2-高 3-中 4-低")
    private Integer priority;

    @ApiModelProperty("分配时间")
    private Date assignTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("完成时间")
    private Date finishTime;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty(hidden = true)
    private String shipmentNo;
    @ApiModelProperty(hidden = true)
    private String pickerName;
    @ApiModelProperty(hidden = true)
    private String statusName;
    @ApiModelProperty(hidden = true)
    private String priorityName;
    @ApiModelProperty(hidden = true)
    private List<PickingTaskDetail> details;
}

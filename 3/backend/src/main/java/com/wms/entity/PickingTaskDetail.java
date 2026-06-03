package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("拣货任务明细")
public class PickingTaskDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("拣货任务ID")
    private Long pickingTaskId;

    @ApiModelProperty("出库分配明细ID")
    private Long shipmentAllocateId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("库位编码")
    private String locationCode;

    @ApiModelProperty("计划数量")
    private BigDecimal planQuantity;

    @ApiModelProperty("已拣数量")
    private BigDecimal pickedQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("是否已拣：0-否 1-是")
    private Integer isPicked;

    @ApiModelProperty("拣货时间")
    private Date pickTime;

    @ApiModelProperty("拣货人")
    private String pickOperator;

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
}

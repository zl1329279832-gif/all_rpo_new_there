package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("盘点单")
public class StocktakeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("盘点单号")
    private String stocktakeNo;

    @ApiModelProperty("盘点类型：1-全盘 2-抽盘 3-循环盘点")
    private Integer stocktakeType;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("库区ID")
    private Long areaId;

    @ApiModelProperty("盘点方式：1-人工盘点 2-扫码盘点 3-PDA盘点")
    private Integer stocktakeMethod;

    @ApiModelProperty("状态：1-新建 2-已确认 3-盘点中 4-差异处理中 5-已完成 6-已取消")
    private Integer status;

    @ApiModelProperty("盘点行数")
    private Integer totalItems;

    @ApiModelProperty("系统总数量")
    private BigDecimal totalQuantity;

    @ApiModelProperty("实盘总数量")
    private BigDecimal countQuantity;

    @ApiModelProperty("盘盈总数量")
    private BigDecimal profitQuantity;

    @ApiModelProperty("盘亏总数量")
    private BigDecimal lossQuantity;

    @ApiModelProperty("确认时间")
    private Date confirmTime;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("完成时间")
    private Date finishTime;

    @ApiModelProperty("盘点人")
    private String handler;

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
    private String areaName;
    @ApiModelProperty(hidden = true)
    private String statusName;
    @ApiModelProperty(hidden = true)
    private List<StocktakeOrderDetail> details;
}

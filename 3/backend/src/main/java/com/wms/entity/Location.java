package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("库位")
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("库区ID")
    private Long areaId;

    @ApiModelProperty("库区编码")
    private String zoneCode;

    @ApiModelProperty("库区名称")
    private String zoneName;

    @ApiModelProperty("库位编码")
    private String locationCode;

    @ApiModelProperty("库位名称")
    private String locationName;

    @ApiModelProperty("库位类型：1-普通位 2-冷藏位 3-危险品位 4-大件位")
    private Integer locationType;

    @ApiModelProperty("排号")
    private Integer rowNum;

    @ApiModelProperty("列号")
    private Integer colNum;

    @ApiModelProperty("列号（别名）")
    private Integer columnNum;

    @ApiModelProperty("层号")
    private Integer layerNum;

    @ApiModelProperty("最大容量")
    private BigDecimal maxCapacity;

    @ApiModelProperty("已用容量")
    private BigDecimal usedCapacity;

    @ApiModelProperty("当前数量")
    private BigDecimal currentQuantity;

    @ApiModelProperty("可用容量")
    private BigDecimal availableCapacity;

    @ApiModelProperty("状态：0-禁用 1-空闲 2-占用 3-锁定")
    private Integer status;

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
    private BigDecimal usageRate;
}

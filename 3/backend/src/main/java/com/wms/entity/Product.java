package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("商品")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("商品编码")
    private String productCode;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("条码")
    private String barcode;

    @ApiModelProperty("规格")
    private String specification;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("品牌")
    private String brand;

    @ApiModelProperty("重量(kg)")
    private BigDecimal weight;

    @ApiModelProperty("体积(m³)")
    private BigDecimal volume;

    @ApiModelProperty("单价")
    private BigDecimal price;

    @ApiModelProperty("保质期(天)")
    private Integer shelfLife;

    @ApiModelProperty("预警天数")
    private Integer warningDays;

    @ApiModelProperty("最低库存")
    private BigDecimal minStock;

    @ApiModelProperty("最高库存")
    private BigDecimal maxStock;

    @ApiModelProperty("存储条件：1-常温 2-冷藏 3-冷冻 4-恒温")
    private Integer storageCondition;

    @ApiModelProperty("状态：0-禁用 1-启用")
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
}

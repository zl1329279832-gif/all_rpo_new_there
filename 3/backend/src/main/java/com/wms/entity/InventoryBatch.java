package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("批次库存")
public class InventoryBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("供应商ID")
    private Long supplierId;

    @ApiModelProperty("库存数量")
    private BigDecimal quantity;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("锁定数量")
    private BigDecimal lockedQuantity;

    @ApiModelProperty("冻结数量")
    private BigDecimal frozenQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("生产日期")
    private Date produceDate;

    @ApiModelProperty("过期日期")
    private Date expireDate;

    @ApiModelProperty("入库日期")
    private Date inboundDate;

    @ApiModelProperty("库存状态：1-正常 2-临期 3-过期 4-冻结")
    private Integer inventoryStatus;

    @ApiModelProperty("质检状态：0-未质检 1-质检中 2-合格 3-不合格")
    private Integer inspectionStatus;

    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

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
    private String productCode;
    @ApiModelProperty(hidden = true)
    private String productName;
    @ApiModelProperty(hidden = true)
    private String locationCode;
    @ApiModelProperty(hidden = true)
    private String supplierName;
    @ApiModelProperty(hidden = true)
    private Integer remainingDays;
}

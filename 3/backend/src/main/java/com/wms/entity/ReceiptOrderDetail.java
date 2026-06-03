package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("入库单明细")
public class ReceiptOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("入库单ID")
    private Long receiptOrderId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("计划数量")
    private BigDecimal planQuantity;

    @ApiModelProperty("到货数量")
    private BigDecimal arrivalQuantity;

    @ApiModelProperty("合格数量")
    private BigDecimal qualifiedQuantity;

    @ApiModelProperty("不合格数量")
    private BigDecimal unqualifiedQuantity;

    @ApiModelProperty("实际入库数量")
    private BigDecimal actualQuantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("生产日期")
    private Date produceDate;

    @ApiModelProperty("过期日期")
    private Date expireDate;

    @ApiModelProperty("质检结果：1-合格 2-不合格")
    private Integer inspectionResult;

    @ApiModelProperty("质检备注")
    private String inspectionRemark;

    @ApiModelProperty("分配库位ID")
    private Long locationId;

    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

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
    private String locationCode;
    @ApiModelProperty(hidden = true)
    private String specification;
}

package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("入库单")
public class ReceiptOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("入库单号")
    private String receiptNo;

    @ApiModelProperty("入库类型：1-采购入库 2-退货入库 3-调拨入库 4-盘盈入库")
    private Integer receiptType;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("供应商ID")
    private Long supplierId;

    @ApiModelProperty("单据状态：1-待到货 2-已到货 3-质检中 4-质检完成 5-待入库 6-入库中 7-已完成 8-已取消")
    private Integer orderStatus;

    @ApiModelProperty("总数量")
    private BigDecimal totalQuantity;

    @ApiModelProperty("实际数量")
    private BigDecimal actualQuantity;

    @ApiModelProperty("合格数量")
    private BigDecimal qualifiedQuantity;

    @ApiModelProperty("不合格数量")
    private BigDecimal unqualifiedQuantity;

    @ApiModelProperty("到货时间")
    private Date arrivalTime;

    @ApiModelProperty("质检时间")
    private Date inspectionTime;

    @ApiModelProperty("完成时间")
    private Date completeTime;

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
    private String supplierName;
    @ApiModelProperty(hidden = true)
    private String receiptTypeName;
    @ApiModelProperty(hidden = true)
    private String orderStatusName;
    @ApiModelProperty(hidden = true)
    private List<ReceiptOrderDetail> details;
}

package com.wms.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("库存预警")
public class InventoryAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("预警单号")
    private String alertNo;

    @ApiModelProperty("预警类型：1-库存不足 2-库存过量 3-效期临期 4-效期过期 5-库位容量不足")
    private Integer alertType;

    @ApiModelProperty("仓库ID")
    private Long warehouseId;

    @ApiModelProperty("库位ID")
    private Long locationId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("当前数量")
    private BigDecimal currentQuantity;

    @ApiModelProperty("当前值")
    private BigDecimal currentValue;

    @ApiModelProperty("阈值数量")
    private BigDecimal thresholdQuantity;

    @ApiModelProperty("阈值")
    private BigDecimal thresholdValue;

    @ApiModelProperty("预警消息")
    private String alertMessage;

    @ApiModelProperty("当前日期")
    private Date currentDate;

    @ApiModelProperty("过期日期")
    private Date expireDate;

    @ApiModelProperty("剩余天数")
    private Integer remainingDays;

    @ApiModelProperty("预警级别：1-低 2-中 3-高")
    private Integer alertLevel;

    @ApiModelProperty("预警时间")
    private Date alertTime;

    @ApiModelProperty("状态：1-待处理 2-处理中 3-已处理 4-已忽略")
    private Integer status;

    @ApiModelProperty("处理人")
    private String handler;

    @ApiModelProperty("处理时间")
    private Date handleTime;

    @ApiModelProperty("处理结果")
    private String handleResult;

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
    private String alertTypeName;
    @ApiModelProperty(hidden = true)
    private String alertLevelName;
    @ApiModelProperty(hidden = true)
    private String statusName;
}

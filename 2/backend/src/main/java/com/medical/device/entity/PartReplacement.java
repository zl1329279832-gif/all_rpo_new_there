package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配件更换记录实体类
 */
@Data
@TableName("part_replacement")
public class PartReplacement {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("repair_order_id")
    private Long repairOrderId;

    @TableField("part_id")
    private Long partId;

    @TableField("part_code")
    private String partCode;

    @TableField("part_name")
    private String partName;

    @TableField("model")
    private String model;

    @TableField("brand")
    private String brand;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("replacement_reason")
    private String replacementReason;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("replacement_time")
    private LocalDateTime replacementTime;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

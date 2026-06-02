package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("part_replacement")
public class PartReplacement {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("repair_order_id")
    private Long repairOrderId;

    @TableField("spare_part_id")
    private Long sparePartId;

    @TableField("part_name")
    private String partName;

    @TableField("part_model")
    private String partModel;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("replace_time")
    private LocalDateTime replaceTime;

    @TableField("operator")
    private String operator;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

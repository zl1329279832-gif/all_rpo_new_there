package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("spare_part")
public class SparePart {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("part_name")
    private String partName;

    @TableField("part_code")
    private String partCode;

    @TableField("part_model")
    private String partModel;

    @TableField("manufacturer")
    private String manufacturer;

    @TableField("unit")
    private String unit;

    @TableField("price")
    private BigDecimal price;

    @TableField("stock_quantity")
    private Integer stockQuantity;

    @TableField("min_stock")
    private Integer minStock;

    @TableField("description")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

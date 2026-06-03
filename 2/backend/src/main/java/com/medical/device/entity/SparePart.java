package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("spare_part")
public class SparePart {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "备件名称不能为空")
    @Size(max = 100, message = "备件名称长度不能超过100个字符")
    @TableField("part_name")
    private String partName;

    @NotBlank(message = "备件编码不能为空")
    @Size(max = 50, message = "备件编码长度不能超过50个字符")
    @TableField("part_code")
    private String partCode;

    @Size(max = 50, message = "备件型号长度不能超过50个字符")
    @TableField("part_model")
    private String partModel;

    @Size(max = 100, message = "生产厂商长度不能超过100个字符")
    @TableField("manufacturer")
    private String manufacturer;

    @Size(max = 20, message = "单位长度不能超过20个字符")
    @TableField("unit")
    private String unit;

    @DecimalMin(value = "0", message = "价格不能小于0")
    @TableField("price")
    private BigDecimal price;

    @Min(value = 0, message = "库存数量不能小于0")
    @TableField("stock_quantity")
    private Integer stockQuantity;

    @Min(value = 0, message = "最小库存不能小于0")
    @TableField("min_stock")
    private Integer minStock;

    @Size(max = 500, message = "描述长度不能超过500个字符")
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

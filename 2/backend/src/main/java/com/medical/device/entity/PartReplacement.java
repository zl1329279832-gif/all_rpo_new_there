package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("part_replacement")
public class PartReplacement {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotNull(message = "维修工单ID不能为空")
    @TableField("repair_order_id")
    private Long repairOrderId;

    @NotNull(message = "备件ID不能为空")
    @TableField("spare_part_id")
    private Long sparePartId;

    @Size(max = 100, message = "配件名称长度不能超过100个字符")
    @TableField("part_name")
    private String partName;

    @Size(max = 50, message = "配件型号长度不能超过50个字符")
    @TableField("part_model")
    private String partModel;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    @Max(value = 10000, message = "数量不能大于10000")
    @TableField("quantity")
    private Integer quantity;

    @DecimalMin(value = "0", message = "单价不能小于0")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0", message = "总价不能小于0")
    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("replace_time")
    private LocalDateTime replaceTime;

    @Size(max = 50, message = "操作人长度不能超过50个字符")
    @TableField("operator")
    private String operator;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

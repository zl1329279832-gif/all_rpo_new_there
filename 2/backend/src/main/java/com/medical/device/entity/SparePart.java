package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 备件实体类
 */
@Data
@TableName("spare_part")
public class SparePart {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("part_code")
    private String partCode;

    @TableField("part_name")
    private String partName;

    @TableField("part_type")
    private String partType;

    @TableField("model")
    private String model;

    @TableField("brand")
    private String brand;

    @TableField("specification")
    private String specification;

    @TableField("unit")
    private String unit;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("stock_quantity")
    private Integer stockQuantity;

    @TableField("min_stock")
    private Integer minStock;

    @TableField("max_stock")
    private Integer maxStock;

    @TableField("supplier")
    private String supplier;

    @TableField("supplier_contact")
    private String supplierContact;

    @TableField("supplier_phone")
    private String supplierPhone;

    @TableField("location")
    private String location;

    @TableField("applicable_devices")
    private String applicableDevices;

    @TableField("remark")
    private String remark;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

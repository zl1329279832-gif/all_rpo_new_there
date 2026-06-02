package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.device.enums.DeviceStatus;
import com.medical.device.enums.RiskLevel;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备实体类
 */
@Data
@TableName("device")
public class Device {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_code")
    private String deviceCode;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private String deviceType;

    @TableField("model")
    private String model;

    @TableField("brand")
    private String brand;

    @TableField("specification")
    private String specification;

    @TableField("serial_number")
    private String serialNumber;

    @TableField("manufacturer")
    private String manufacturer;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    @TableField("department_id")
    private Long departmentId;

    @TableField("location")
    private String location;

    @TableField("responsible_person")
    private String responsiblePerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("risk_level")
    private RiskLevel riskLevel;

    @TableField("status")
    private DeviceStatus status;

    @TableField("warranty_start_date")
    private LocalDate warrantyStartDate;

    @TableField("warranty_end_date")
    private LocalDate warrantyEndDate;

    @TableField("last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @TableField("next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @TableField("last_calibration_date")
    private LocalDate lastCalibrationDate;

    @TableField("next_calibration_date")
    private LocalDate nextCalibrationDate;

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

package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_code")
    private String deviceCode;

    @TableField("device_type")
    private String deviceType;

    @TableField("device_model")
    private String deviceModel;

    @TableField("manufacturer")
    private String manufacturer;

    @TableField("serial_number")
    private String serialNumber;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    @TableField("warranty_start")
    private LocalDate warrantyStart;

    @TableField("warranty_end")
    private LocalDate warrantyEnd;

    @TableField("dept_id")
    private Long deptId;

    @TableField("location")
    private String location;

    @TableField("status")
    private Integer status;

    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @TableField("next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @TableField("total_downtime")
    private Integer totalDowntime;

    @TableField("maintainer")
    private String maintainer;

    @TableField("description")
    private String description;

    @TableField("qc_status")
    private Integer qcStatus;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

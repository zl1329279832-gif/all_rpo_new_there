package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("device")
public class Device {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @TableField("device_name")
    private String deviceName;

    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    @TableField("device_code")
    private String deviceCode;

    @Size(max = 50, message = "设备类型长度不能超过50个字符")
    @TableField("device_type")
    private String deviceType;

    @Size(max = 50, message = "设备型号长度不能超过50个字符")
    @TableField("device_model")
    private String deviceModel;

    @Size(max = 100, message = "生产厂商长度不能超过100个字符")
    @TableField("manufacturer")
    private String manufacturer;

    @Size(max = 100, message = "序列号长度不能超过100个字符")
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

    @Size(max = 200, message = "位置长度不能超过200个字符")
    @TableField("location")
    private String location;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 10, message = "状态值不能大于10")
    @TableField("status")
    private Integer status;

    @Min(value = 0, message = "风险等级不能小于0")
    @Max(value = 5, message = "风险等级不能大于5")
    @TableField("risk_level")
    private Integer riskLevel;

    @TableField("last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @TableField("next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Min(value = 0, message = "总停机时间不能小于0")
    @TableField("total_downtime")
    private Integer totalDowntime;

    @Size(max = 50, message = "维护人员长度不能超过50个字符")
    @TableField("maintainer")
    private String maintainer;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    @Min(value = 0, message = "质控状态值不能小于0")
    @Max(value = 5, message = "质控状态值不能大于5")
    @TableField("qc_status")
    private Integer qcStatus;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String deptName;
}

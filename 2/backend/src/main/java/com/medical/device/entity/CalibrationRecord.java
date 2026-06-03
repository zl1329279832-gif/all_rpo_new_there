package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("calibration_record")
public class CalibrationRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @Size(max = 50, message = "校准类型长度不能超过50个字符")
    @TableField("calibration_type")
    private String calibrationType;

    @NotNull(message = "校准日期不能为空")
    @TableField("calibration_date")
    private LocalDate calibrationDate;

    @Size(max = 100, message = "校准机构长度不能超过100个字符")
    @TableField("calibration_agency")
    private String calibrationAgency;

    @Size(max = 50, message = "校准人员长度不能超过50个字符")
    @TableField("calibration_person")
    private String calibrationPerson;

    @Size(max = 100, message = "证书编号长度不能超过100个字符")
    @TableField("certificate_number")
    private String certificateNumber;

    @TableField("valid_until")
    private LocalDate validUntil;

    @Min(value = 0, message = "校准结果值不能小于0")
    @Max(value = 3, message = "校准结果值不能大于3")
    @TableField("calibration_result")
    private Integer calibrationResult;

    @Size(max = 1000, message = "校准项目长度不能超过1000个字符")
    @TableField("calibration_items")
    private String calibrationItems;

    @DecimalMin(value = "0", message = "偏差值不能小于0")
    @TableField("deviation_value")
    private BigDecimal deviationValue;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    @TableField("next_calibration_date")
    private LocalDate nextCalibrationDate;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

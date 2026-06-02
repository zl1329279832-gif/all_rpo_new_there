package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("calibration_record")
public class CalibrationRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    @TableField("calibration_type")
    private String calibrationType;

    @TableField("calibration_date")
    private LocalDate calibrationDate;

    @TableField("calibration_agency")
    private String calibrationAgency;

    @TableField("calibration_person")
    private String calibrationPerson;

    @TableField("certificate_number")
    private String certificateNumber;

    @TableField("valid_until")
    private LocalDate validUntil;

    @TableField("calibration_result")
    private Integer calibrationResult;

    @TableField("calibration_items")
    private String calibrationItems;

    @TableField("deviation_value")
    private BigDecimal deviationValue;

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

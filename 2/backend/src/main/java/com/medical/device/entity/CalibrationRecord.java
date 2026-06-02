package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 校准记录实体类
 */
@Data
@TableName("calibration_record")
public class CalibrationRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("record_code")
    private String recordCode;

    @TableField("device_id")
    private Long deviceId;

    @TableField("calibration_type")
    private Integer calibrationType;

    @TableField("calibration_date")
    private LocalDate calibrationDate;

    @TableField("calibration_agency")
    private String calibrationAgency;

    @TableField("calibrator")
    private String calibrator;

    @TableField("calibration_items")
    private String calibrationItems;

    @TableField("calibration_standard")
    private String calibrationStandard;

    @TableField("calibration_result")
    private Integer calibrationResult;

    @TableField("certificate_number")
    private String certificateNumber;

    @TableField("validity_date")
    private LocalDate validityDate;

    @TableField("next_calibration_date")
    private LocalDate nextCalibrationDate;

    @TableField("calibration_cost")
    private BigDecimal calibrationCost;

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

package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qc_record")
public class QcRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @TableField("device_id")
    private Long deviceId;

    @TableField("qc_date")
    private LocalDate qcDate;

    @TableField("qc_type")
    private Integer qcType;

    @TableField("executor_id")
    private Long executorId;

    @TableField("executor_name")
    private String executorName;

    @TableField("qc_result")
    private Integer qcResult;

    @TableField("qc_data")
    private String qcData;

    @TableField("deviation_description")
    private String deviationDescription;

    @TableField("handle_measure")
    private String handleMeasure;

    @TableField("recheck_date")
    private LocalDate recheckDate;

    @TableField("recheck_result")
    private Integer recheckResult;

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

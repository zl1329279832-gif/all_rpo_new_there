package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qc_plan")
public class QcPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_name")
    private String planName;

    @TableField("device_id")
    private Long deviceId;

    @TableField("qc_type")
    private Integer qcType;

    @TableField("cycle_type")
    private Integer cycleType;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("next_execution_date")
    private LocalDate nextExecutionDate;

    @TableField("qc_standard")
    private String qcStandard;

    @TableField("qc_items")
    private String qcItems;

    @TableField("executor")
    private String executor;

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

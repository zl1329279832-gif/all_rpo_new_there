package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_task")
public class InspectionTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_code")
    private String taskCode;

    @TableField("plan_id")
    private Long planId;

    @TableField("device_id")
    private Long deviceId;

    @TableField("task_name")
    private String taskName;

    @TableField("task_type")
    private Integer taskType;

    @TableField("plan_date")
    private LocalDate planDate;

    @TableField("actual_date")
    private LocalDateTime actualDate;

    @TableField("inspector_id")
    private Long inspectorId;

    @TableField("inspector_name")
    private String inspectorName;

    @TableField("status")
    private Integer status;

    @TableField("check_result")
    private Integer checkResult;

    @TableField("abnormal_description")
    private String abnormalDescription;

    @TableField("handle_suggestion")
    private String handleSuggestion;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

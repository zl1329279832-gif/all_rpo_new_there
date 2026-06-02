package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.device.enums.InspectionTaskStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 巡检任务实体类
 */
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

    @TableField("inspector_id")
    private Long inspectorId;

    @TableField("plan_start_time")
    private LocalDateTime planStartTime;

    @TableField("plan_end_time")
    private LocalDateTime planEndTime;

    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    @TableField("status")
    private InspectionTaskStatus status;

    @TableField("inspection_result")
    private String inspectionResult;

    @TableField("inspection_images")
    private String inspectionImages;

    @TableField("has_exception")
    private Integer hasException;

    @TableField("exception_desc")
    private String exceptionDesc;

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

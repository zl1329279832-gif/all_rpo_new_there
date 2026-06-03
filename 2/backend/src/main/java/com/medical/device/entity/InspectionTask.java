package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_task")
public class InspectionTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Size(max = 50, message = "任务编码长度不能超过50个字符")
    @TableField("task_code")
    private String taskCode;

    @TableField("plan_id")
    private Long planId;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    @TableField("task_name")
    private String taskName;

    @Min(value = 0, message = "任务类型值不能小于0")
    @Max(value = 10, message = "任务类型值不能大于10")
    @TableField("task_type")
    private Integer taskType;

    @NotNull(message = "计划日期不能为空")
    @TableField("plan_date")
    private LocalDate planDate;

    @TableField("actual_date")
    private LocalDateTime actualDate;

    @TableField("inspector_id")
    private Long inspectorId;

    @Size(max = 50, message = "检查人员姓名长度不能超过50个字符")
    @TableField("inspector_name")
    private String inspectorName;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 5, message = "状态值不能大于5")
    @TableField("status")
    private Integer status;

    @Min(value = 0, message = "检查结果值不能小于0")
    @Max(value = 3, message = "检查结果值不能大于3")
    @TableField("check_result")
    private Integer checkResult;

    @Size(max = 500, message = "异常描述长度不能超过500个字符")
    @TableField("abnormal_description")
    private String abnormalDescription;

    @Size(max = 500, message = "处理建议长度不能超过500个字符")
    @TableField("handle_suggestion")
    private String handleSuggestion;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String deviceName;

    @TableField(exist = false)
    private String deviceCode;
}

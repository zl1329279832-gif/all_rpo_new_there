package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_plan")
public class InspectionPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 100, message = "计划名称长度不能超过100个字符")
    @TableField("plan_name")
    private String planName;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @NotNull(message = "周期类型不能为空")
    @Min(value = 0, message = "周期类型值不能小于0")
    @Max(value = 10, message = "周期类型值不能大于10")
    @TableField("cycle_type")
    private Integer cycleType;

    @Min(value = 1, message = "周期天数不能小于1")
    @Max(value = 3650, message = "周期天数不能大于3650")
    @TableField("cycle_days")
    private Integer cycleDays;

    @NotNull(message = "开始日期不能为空")
    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("next_execution_date")
    private LocalDate nextExecutionDate;

    @Size(max = 50, message = "检查人员长度不能超过50个字符")
    @TableField("inspector")
    private String inspector;

    @Size(max = 2000, message = "检查项目长度不能超过2000个字符")
    @TableField("check_items")
    private String checkItems;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 10, message = "状态值不能大于10")
    @TableField("status")
    private Integer status;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

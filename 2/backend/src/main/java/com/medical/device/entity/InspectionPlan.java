package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("inspection_plan")
public class InspectionPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_name")
    private String planName;

    @TableField("device_id")
    private Long deviceId;

    @TableField("cycle_type")
    private Integer cycleType;

    @TableField("cycle_days")
    private Integer cycleDays;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("next_execution_date")
    private LocalDate nextExecutionDate;

    @TableField("inspector")
    private String inspector;

    @TableField("check_items")
    private String checkItems;

    @TableField("status")
    private Integer status;

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

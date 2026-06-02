package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检计划实体类
 */
@Data
@TableName("inspection_plan")
public class InspectionPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_code")
    private String planCode;

    @TableField("plan_name")
    private String planName;

    @TableField("plan_type")
    private Integer planType;

    @TableField("device_id")
    private Long deviceId;

    @TableField("department_id")
    private Long departmentId;

    @TableField("cycle_type")
    private Integer cycleType;

    @TableField("cycle_value")
    private Integer cycleValue;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("inspector_id")
    private Long inspectorId;

    @TableField("inspection_items")
    private String inspectionItems;

    @TableField("remark")
    private String remark;

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

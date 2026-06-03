package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qc_plan")
public class QcPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 100, message = "计划名称长度不能超过100个字符")
    @TableField("plan_name")
    private String planName;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @Min(value = 0, message = "质控类型值不能小于0")
    @Max(value = 10, message = "质控类型值不能大于10")
    @TableField("qc_type")
    private Integer qcType;

    @NotNull(message = "周期类型不能为空")
    @Min(value = 0, message = "周期类型值不能小于0")
    @Max(value = 10, message = "周期类型值不能大于10")
    @TableField("cycle_type")
    private Integer cycleType;

    @NotNull(message = "开始日期不能为空")
    @TableField("start_date")
    private LocalDate startDate;

    @TableField("next_execution_date")
    private LocalDate nextExecutionDate;

    @Size(max = 2000, message = "质控标准长度不能超过2000个字符")
    @TableField("qc_standard")
    private String qcStandard;

    @Size(max = 2000, message = "质控项目长度不能超过2000个字符")
    @TableField("qc_items")
    private String qcItems;

    @Size(max = 50, message = "执行人长度不能超过50个字符")
    @TableField("executor")
    private String executor;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 10, message = "状态值不能大于10")
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

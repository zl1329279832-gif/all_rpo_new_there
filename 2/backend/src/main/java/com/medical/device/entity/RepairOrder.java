package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Size(max = 50, message = "工单编码长度不能超过50个字符")
    @TableField("order_code")
    private String orderCode;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @Size(max = 50, message = "故障类型长度不能超过50个字符")
    @TableField("fault_type")
    private String faultType;

    @NotBlank(message = "故障描述不能为空")
    @Size(max = 1000, message = "故障描述长度不能超过1000个字符")
    @TableField("fault_description")
    private String faultDescription;

    @NotNull(message = "故障等级不能为空")
    @Min(value = 1, message = "故障等级不能小于1")
    @Max(value = 5, message = "故障等级不能大于5")
    @TableField("fault_level")
    private Integer faultLevel;

    @TableField("reporter_id")
    private Long reporterId;

    @Size(max = 50, message = "报修人姓名长度不能超过50个字符")
    @TableField("reporter_name")
    private String reporterName;

    @TableField("report_time")
    private LocalDateTime reportTime;

    @TableField("repairer_id")
    private Long repairerId;

    @Size(max = 50, message = "维修人员姓名长度不能超过50个字符")
    @TableField("repairer_name")
    private String repairerName;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 10, message = "状态值不能大于10")
    @TableField("status")
    private Integer status;

    @Size(max = 2000, message = "维修内容长度不能超过2000个字符")
    @TableField("repair_content")
    private String repairContent;

    @Size(max = 500, message = "维修结果长度不能超过500个字符")
    @TableField("repair_result")
    private String repairResult;

    @Min(value = 0, message = "停机时间不能小于0")
    @TableField("downtime")
    private Integer downtime;

    @DecimalMin(value = "0", message = "维修费用不能小于0")
    @TableField("repair_cost")
    private BigDecimal repairCost;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    private String remark;

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

    @TableField(exist = false)
    private String statusName;
}

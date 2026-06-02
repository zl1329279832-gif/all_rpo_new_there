package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_code")
    private String orderCode;

    @TableField("device_id")
    private Long deviceId;

    @TableField("fault_type")
    private Integer faultType;

    @TableField("fault_description")
    private String faultDescription;

    @TableField("fault_level")
    private Integer faultLevel;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("reporter_name")
    private String reporterName;

    @TableField("report_time")
    private LocalDateTime reportTime;

    @TableField("repairer_id")
    private Long repairerId;

    @TableField("repairer_name")
    private String repairerName;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField("status")
    private Integer status;

    @TableField("repair_content")
    private String repairContent;

    @TableField("repair_result")
    private String repairResult;

    @TableField("downtime")
    private Integer downtime;

    @TableField("repair_cost")
    private BigDecimal repairCost;

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

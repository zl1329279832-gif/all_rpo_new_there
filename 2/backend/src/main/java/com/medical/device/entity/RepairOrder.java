package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.device.enums.RepairOrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单实体类
 */
@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_code")
    private String orderCode;

    @TableField("device_id")
    private Long deviceId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("reporter_name")
    private String reporterName;

    @TableField("reporter_phone")
    private String reporterPhone;

    @TableField("fault_type")
    private Integer faultType;

    @TableField("fault_level")
    private Integer faultLevel;

    @TableField("fault_desc")
    private String faultDesc;

    @TableField("fault_images")
    private String faultImages;

    @TableField("repairer_id")
    private Long repairerId;

    @TableField("repairer_name")
    private String repairerName;

    @TableField("status")
    private RepairOrderStatus status;

    @TableField("plan_start_time")
    private LocalDateTime planStartTime;

    @TableField("plan_end_time")
    private LocalDateTime planEndTime;

    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    @TableField("repair_content")
    private String repairContent;

    @TableField("repair_result")
    private String repairResult;

    @TableField("repair_cost")
    private BigDecimal repairCost;

    @TableField("need_spare_parts")
    private Integer needSpareParts;

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

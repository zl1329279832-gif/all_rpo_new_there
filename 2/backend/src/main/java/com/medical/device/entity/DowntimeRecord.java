package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 停机记录实体类
 */
@Data
@TableName("downtime_record")
public class DowntimeRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("record_code")
    private String recordCode;

    @TableField("device_id")
    private Long deviceId;

    @TableField("downtime_type")
    private Integer downtimeType;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("downtime_duration")
    private BigDecimal downtimeDuration;

    @TableField("downtime_reason")
    private String downtimeReason;

    @TableField("related_order_id")
    private Long relatedOrderId;

    @TableField("related_order_type")
    private Integer relatedOrderType;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("impact_description")
    private String impactDescription;

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

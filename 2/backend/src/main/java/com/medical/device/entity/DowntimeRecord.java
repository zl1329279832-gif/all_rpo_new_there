package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("downtime_record")
public class DowntimeRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @TableField("repair_order_id")
    private Long repairOrderId;

    @NotNull(message = "停机类型不能为空")
    @Min(value = 0, message = "停机类型值不能小于0")
    @Max(value = 10, message = "停机类型值不能大于10")
    @TableField("downtime_type")
    private Integer downtimeType;

    @NotNull(message = "开始时间不能为空")
    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @Min(value = 0, message = "持续时间不能小于0")
    @TableField("duration")
    private Integer duration;

    @Size(max = 200, message = "原因长度不能超过200个字符")
    @TableField("reason")
    private String reason;

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

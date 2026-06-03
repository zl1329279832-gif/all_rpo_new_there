package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qc_record")
public class QcRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("plan_id")
    private Long planId;

    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    @NotNull(message = "质控日期不能为空")
    @TableField("qc_date")
    private LocalDate qcDate;

    @Size(max = 100, message = "质控类型长度不能超过100个字符")
    @TableField("qc_type")
    private String qcType;

    @TableField("executor_id")
    private Long executorId;

    @Size(max = 50, message = "执行人员姓名长度不能超过50个字符")
    @TableField("executor_name")
    private String executorName;

    @NotNull(message = "质控结果不能为空")
    @Min(value = 0, message = "质控结果值不能小于0")
    @Max(value = 3, message = "质控结果值不能大于3")
    @TableField("qc_result")
    private Integer qcResult;

    @Size(max = 2000, message = "质控数据长度不能超过2000个字符")
    @TableField("qc_data")
    private String qcData;

    @Size(max = 500, message = "偏差描述长度不能超过500个字符")
    @TableField("deviation_description")
    private String deviationDescription;

    @Size(max = 500, message = "处理措施长度不能超过500个字符")
    @TableField("handle_measure")
    private String handleMeasure;

    @TableField("recheck_date")
    private LocalDate recheckDate;

    @Min(value = 0, message = "复检结果值不能小于0")
    @Max(value = 3, message = "复检结果值不能大于3")
    @TableField("recheck_result")
    private Integer recheckResult;

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
}

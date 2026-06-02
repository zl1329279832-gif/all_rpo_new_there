package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.device.enums.QcResult;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 质检记录实体类
 */
@Data
@TableName("qc_record")
public class QcRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("record_code")
    private String recordCode;

    @TableField("plan_id")
    private Long planId;

    @TableField("device_id")
    private Long deviceId;

    @TableField("qc_person_id")
    private Long qcPersonId;

    @TableField("qc_person_name")
    private String qcPersonName;

    @TableField("plan_start_time")
    private LocalDateTime planStartTime;

    @TableField("plan_end_time")
    private LocalDateTime planEndTime;

    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    @TableField("qc_items")
    private String qcItems;

    @TableField("qc_result")
    private QcResult qcResult;

    @TableField("qc_desc")
    private String qcDesc;

    @TableField("qc_images")
    private String qcImages;

    @TableField("has_problem")
    private Integer hasProblem;

    @TableField("problem_desc")
    private String problemDesc;

    @TableField("handle_suggestion")
    private String handleSuggestion;

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

package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_name")
    private String deptName;

    @TableField("dept_code")
    private String deptCode;

    @TableField("parent_id")
    private Long parentId;

    @TableField("leader")
    private String leader;

    @TableField("phone")
    private String phone;

    @TableField("address")
    private String address;

    @TableField("sort_order")
    private Integer sortOrder;

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

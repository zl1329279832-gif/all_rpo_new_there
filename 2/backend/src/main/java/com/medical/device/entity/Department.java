package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("department")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @TableField("dept_name")
    private String deptName;

    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    @TableField("dept_code")
    private String deptCode;

    @TableField("parent_id")
    private Long parentId;

    @Size(max = 50, message = "负责人长度不能超过50个字符")
    @TableField("leader")
    private String leader;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @TableField("phone")
    private String phone;

    @Size(max = 200, message = "地址长度不能超过200个字符")
    @TableField("address")
    private String address;

    @Min(value = 0, message = "排序值不能小于0")
    @Max(value = 10000, message = "排序值不能大于10000")
    @TableField("sort_order")
    private Integer sortOrder;

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

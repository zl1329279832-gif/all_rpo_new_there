package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_contract")
public class MaintenanceContract {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Size(max = 50, message = "合同编码长度不能超过50个字符")
    @TableField("contract_code")
    private String contractCode;

    @NotBlank(message = "合同名称不能为空")
    @Size(max = 100, message = "合同名称长度不能超过100个字符")
    @TableField("contract_name")
    private String contractName;

    @Min(value = 0, message = "合同类型值不能小于0")
    @Max(value = 10, message = "合同类型值不能大于10")
    @TableField("contract_type")
    private Integer contractType;

    @TableField("device_id")
    private Long deviceId;

    @Size(max = 100, message = "供应商长度不能超过100个字符")
    @TableField("supplier")
    private String supplier;

    @Size(max = 50, message = "联系人长度不能超过50个字符")
    @TableField("contact_person")
    private String contactPerson;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @TableField("contact_phone")
    private String contactPhone;

    @NotNull(message = "合同开始日期不能为空")
    @TableField("start_date")
    private LocalDate startDate;

    @NotNull(message = "合同结束日期不能为空")
    @TableField("end_date")
    private LocalDate endDate;

    @DecimalMin(value = "0", message = "合同金额不能小于0")
    @TableField("contract_amount")
    private BigDecimal contractAmount;

    @Min(value = 0, message = "付款状态值不能小于0")
    @Max(value = 5, message = "付款状态值不能大于5")
    @TableField("payment_status")
    private Integer paymentStatus;

    @Min(value = 0, message = "状态值不能小于0")
    @Max(value = 10, message = "状态值不能大于10")
    @TableField("status")
    private Integer status;

    @Size(max = 2000, message = "合同内容长度不能超过2000个字符")
    @TableField("content")
    private String content;

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
}

package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_contract")
public class MaintenanceContract {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("contract_code")
    private String contractCode;

    @TableField("contract_name")
    private String contractName;

    @TableField("contract_type")
    private Integer contractType;

    @TableField("device_id")
    private Long deviceId;

    @TableField("supplier")
    private String supplier;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("contract_amount")
    private BigDecimal contractAmount;

    @TableField("payment_status")
    private Integer paymentStatus;

    @TableField("status")
    private Integer status;

    @TableField("content")
    private String content;

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

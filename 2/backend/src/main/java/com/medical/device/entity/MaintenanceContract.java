package com.medical.device.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维保合同实体类
 */
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

    @TableField("device_ids")
    private String deviceIds;

    @TableField("supplier_id")
    private Long supplierId;

    @TableField("supplier_name")
    private String supplierName;

    @TableField("contract_amount")
    private BigDecimal contractAmount;

    @TableField("payment_method")
    private Integer paymentMethod;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("maintenance_frequency")
    private Integer maintenanceFrequency;

    @TableField("service_content")
    private String serviceContent;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contract_status")
    private Integer contractStatus;

    @TableField("attachment")
    private String attachment;

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

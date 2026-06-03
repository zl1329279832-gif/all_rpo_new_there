package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeviceDTO {

    private Long id;

    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    private String deviceModel;

    private String serialNumber;

    private String manufacturer;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    @NotNull(message = "所属科室不能为空")
    private Long deptId;

    private String location;

    private Integer status;

    private Integer riskLevel;

    private Integer qcStatus;

    private LocalDate warrantyStart;

    private LocalDate warrantyEnd;

    private LocalDate nextMaintenanceDate;

    private LocalDate nextCalibrationDate;

    private Integer totalDowntime;

    private String maintainer;

    private String description;
}

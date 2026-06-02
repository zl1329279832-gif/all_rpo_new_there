package com.medical.device.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeviceDTO {

    private Long id;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String deviceModel;

    private String serialNumber;

    private String manufacturer;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

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

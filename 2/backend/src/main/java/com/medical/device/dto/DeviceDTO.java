package com.medical.device.dto;

import com.medical.device.enums.DeviceStatus;
import com.medical.device.enums.RiskLevel;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备DTO
 */
@Data
public class DeviceDTO {

    private Long id;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String model;

    private String brand;

    private String specification;

    private String serialNumber;

    private String manufacturer;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    private Long departmentId;

    private String location;

    private String responsiblePerson;

    private String contactPhone;

    private RiskLevel riskLevel;

    private DeviceStatus status;

    private LocalDate warrantyStartDate;

    private LocalDate warrantyEndDate;

    private LocalDate nextMaintenanceDate;

    private LocalDate nextCalibrationDate;

    private String remark;
}

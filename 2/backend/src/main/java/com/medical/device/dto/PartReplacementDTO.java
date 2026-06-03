package com.medical.device.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 配件更换DTO
 */
@Data
public class PartReplacementDTO {

    private Long sparePartId;

    @NotBlank(message = "配件名称不能为空")
    private String partName;

    private String partModel;

    @NotNull(message = "更换数量不能为空")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private String operator;

    private String remark;
}

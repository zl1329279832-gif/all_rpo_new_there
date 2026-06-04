package com.bakery.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockDamageDTO {

    private Long id;

    private String damageNo;

    private Long storeId;

    private Integer damageType;

    private Integer status;

    private BigDecimal totalQty;

    private BigDecimal totalAmount;

    private String reason;

    private LocalDateTime createTime;

    private LocalDateTime auditTime;

    private String auditOpinion;

    private List<DamageDetailDTO> details;

    @Data
    public static class DamageDetailDTO {
        private Long id;
        private Long batchId;
        private Long recipeId;
        private BigDecimal damageQty;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String batchNo;
        private String productName;
        private LocalDateTime expireTime;
    }
}

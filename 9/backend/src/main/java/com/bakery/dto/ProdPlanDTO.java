package com.bakery.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProdPlanDTO {

    private Long id;

    private String planNo;

    private LocalDate planDate;

    private Long storeId;

    private Integer status;

    private String remark;

    private List<PlanDetailDTO> details;

    @Data
    public static class PlanDetailDTO {
        private Long id;
        private Long recipeId;
        private BigDecimal planQty;
        private BigDecimal actualQty;
        private String productName;
        private String productCode;
        private String unit;
    }
}

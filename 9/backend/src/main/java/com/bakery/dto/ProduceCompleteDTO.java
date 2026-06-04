package com.bakery.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProduceCompleteDTO {

    private Long planId;

    private List<ProduceItem> items;

    @Data
    public static class ProduceItem {
        private Long planDetailId;
        private Long recipeId;
        private BigDecimal actualQty;
        private LocalDateTime produceTime;
    }
}

package com.bakery.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockTransferDTO {

    private Long id;

    private String transferNo;

    private Long outStoreId;

    private Long inStoreId;

    private Integer transferType;

    private Integer status;

    private BigDecimal totalQty;

    private String remark;

    private LocalDateTime outboundTime;

    private LocalDateTime inboundTime;

    private List<TransferDetailDTO> details;

    @Data
    public static class TransferDetailDTO {
        private Long id;
        private Long batchId;
        private Long recipeId;
        private BigDecimal transferQty;
        private String batchNo;
        private String productName;
        private LocalDateTime expireTime;
        private BigDecimal remainQty;
    }
}

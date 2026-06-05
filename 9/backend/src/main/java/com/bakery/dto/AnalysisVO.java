package com.bakery.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AnalysisVO {

    private BigDecimal salesAmount;
    private BigDecimal costAmount;
    private BigDecimal profit;
    private BigDecimal salesQty;
    private BigDecimal damageAmount;
    private BigDecimal damageRate;
    private BigDecimal stockAmount;

    private List<SalesTrendVO> salesTrend;
    private List<CategorySalesVO> categorySales;
    private List<StoreSalesVO> storeSales;
    private List<ProductRankVO> productRank;
    private List<DamageTrendVO> damageTrend;
    private WarningStatsVO warningStats;
    private List<WarningByProductVO> warningByProduct;

    @Data
    public static class WarningByProductVO {
        private String productName;
        private BigDecimal warningQty;
        private BigDecimal expiredQty;
    }

    @Data
    public static class SalesTrendVO {
        private String date;
        private BigDecimal salesAmount;
        private BigDecimal costAmount;
        private BigDecimal profit;
    }

    @Data
    public static class CategorySalesVO {
        private String category;
        private BigDecimal salesAmount;
        private BigDecimal salesQty;
        private BigDecimal ratio;
    }

    @Data
    public static class StoreSalesVO {
        private String storeName;
        private BigDecimal salesAmount;
        private BigDecimal profit;
    }

    @Data
    public static class ProductRankVO {
        private String productName;
        private BigDecimal salesQty;
        private BigDecimal salesAmount;
    }

    @Data
    public static class DamageTrendVO {
        private String date;
        private BigDecimal damageQty;
        private BigDecimal damageAmount;
    }

    @Data
    public static class WarningStatsVO {
        private Integer totalBatches;
        private Integer warningBatches;
        private Integer expiredBatches;
        private BigDecimal totalRemainQty;
        private BigDecimal warningQty;
        private BigDecimal expiredQty;
        private Integer warningProducts;
    }
}

package com.bakery.service;

import com.bakery.dto.AnalysisVO;
import com.bakery.mapper.SalesStatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AnalysisService {

    @Autowired
    private SalesStatMapper salesStatMapper;
    @Autowired
    private ProdBatchService batchService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ANALYSIS_CACHE_KEY = "bakery:analysis:data";

    public AnalysisVO getAnalysisData(LocalDate startDate, LocalDate endDate, Long storeId) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        String cacheKey = ANALYSIS_CACHE_KEY + ":" + startDate + ":" + endDate + ":" + storeId;
        AnalysisVO cached = (AnalysisVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        AnalysisVO vo = new AnalysisVO();

        BigDecimal totalSalesAmount = salesStatMapper.getTotalSalesAmount(startDate, endDate);
        BigDecimal totalCostAmount = salesStatMapper.getTotalCostAmount(startDate, endDate);
        BigDecimal totalSalesQty = salesStatMapper.getTotalSalesQty(startDate, endDate);
        BigDecimal totalDamageAmount = salesStatMapper.getTotalDamageAmount(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        vo.setTotalSalesAmount(totalSalesAmount);
        vo.setTotalCostAmount(totalCostAmount);
        vo.setTotalProfit(totalSalesAmount.subtract(totalCostAmount));
        vo.setTotalSalesQty(totalSalesQty);
        vo.setTotalDamageAmount(totalDamageAmount);

        if (totalSalesAmount.compareTo(BigDecimal.ZERO) > 0) {
            vo.setDamageRate(totalDamageAmount.divide(totalSalesAmount.add(totalDamageAmount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            vo.setDamageRate(BigDecimal.ZERO);
        }

        List<AnalysisVO.SalesTrendVO> salesTrend = salesStatMapper.getSalesTrend(startDate, endDate);
        vo.setSalesTrend(salesTrend);

        List<AnalysisVO.CategorySalesVO> categorySales = salesStatMapper.getCategorySales(startDate, endDate);
        for (AnalysisVO.CategorySalesVO cat : categorySales) {
            if (totalSalesAmount.compareTo(BigDecimal.ZERO) > 0) {
                cat.setRatio(cat.getSalesAmount().divide(totalSalesAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")));
            } else {
                cat.setRatio(BigDecimal.ZERO);
            }
        }
        vo.setCategorySales(categorySales);

        vo.setStoreSales(salesStatMapper.getStoreSales(startDate, endDate));
        vo.setProductRank(salesStatMapper.getProductRank(startDate, endDate));
        vo.setDamageTrend(salesStatMapper.getDamageTrend(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
        vo.setWarningStats(batchService.getWarningStats(storeId != null ? storeId : 1L));

        redisTemplate.opsForValue().set(cacheKey, vo, 30, TimeUnit.MINUTES);
        return vo;
    }

    public void clearAnalysisCache() {
        redisTemplate.delete(redisTemplate.keys(ANALYSIS_CACHE_KEY + ":*"));
    }
}

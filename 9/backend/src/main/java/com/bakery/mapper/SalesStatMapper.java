package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.dto.AnalysisVO;
import com.bakery.entity.SalesStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SalesStatMapper extends BaseMapper<SalesStat> {

    @Select("SELECT COALESCE(SUM(sales_amount), 0) FROM sales_stat WHERE stat_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal getTotalSalesAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(cost_amount), 0) FROM sales_stat WHERE stat_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal getTotalCostAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(sales_qty), 0) FROM sales_stat WHERE stat_date BETWEEN #{startDate} AND #{endDate}")
    BigDecimal getTotalSalesQty(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT DATE_FORMAT(stat_date, '%Y-%m-%d') as date, " +
            "COALESCE(SUM(sales_amount), 0) as salesAmount, " +
            "COALESCE(SUM(cost_amount), 0) as costAmount, " +
            "COALESCE(SUM(sales_amount - cost_amount), 0) as profit " +
            "FROM sales_stat WHERE stat_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY stat_date ORDER BY stat_date ASC")
    List<AnalysisVO.SalesTrendVO> getSalesTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT r.category, " +
            "COALESCE(SUM(s.sales_amount), 0) as salesAmount, " +
            "COALESCE(SUM(s.sales_qty), 0) as salesQty " +
            "FROM sales_stat s " +
            "LEFT JOIN base_recipe r ON s.recipe_id = r.id " +
            "WHERE s.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY r.category ORDER BY salesAmount DESC")
    List<AnalysisVO.CategorySalesVO> getCategorySales(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT st.store_name, " +
            "COALESCE(SUM(s.sales_amount), 0) as salesAmount, " +
            "COALESCE(SUM(s.sales_amount - s.cost_amount), 0) as profit " +
            "FROM sales_stat s " +
            "LEFT JOIN sys_store st ON s.store_id = st.id " +
            "WHERE s.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY s.store_id ORDER BY salesAmount DESC")
    List<AnalysisVO.StoreSalesVO> getStoreSales(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT r.product_name, " +
            "COALESCE(SUM(s.sales_qty), 0) as salesQty, " +
            "COALESCE(SUM(s.sales_amount), 0) as salesAmount " +
            "FROM sales_stat s " +
            "LEFT JOIN base_recipe r ON s.recipe_id = r.id " +
            "WHERE s.stat_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY s.recipe_id ORDER BY salesQty DESC LIMIT 10")
    List<AnalysisVO.ProductRankVO> getProductRank(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT DATE_FORMAT(d.create_time, '%Y-%m-%d') as date, " +
            "COALESCE(SUM(d.total_qty), 0) as damageQty, " +
            "COALESCE(SUM(d.total_amount), 0) as damageAmount " +
            "FROM stock_damage d " +
            "WHERE d.status = 1 AND d.create_time >= #{startDate} AND d.create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY DATE_FORMAT(d.create_time, '%Y-%m-%d') ORDER BY date ASC")
    List<AnalysisVO.DamageTrendVO> getDamageTrend(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM stock_damage WHERE status = 1 AND create_time >= #{startDate} AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY)")
    BigDecimal getTotalDamageAmount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

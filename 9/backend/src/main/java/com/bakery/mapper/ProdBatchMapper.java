package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bakery.entity.ProdBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ProdBatchMapper extends BaseMapper<ProdBatch> {

    @Select("<script>" +
            "SELECT b.*, r.product_name, r.product_code, r.unit, r.category, s.store_name " +
            "FROM prod_batch b " +
            "LEFT JOIN base_recipe r ON b.recipe_id = r.id " +
            "LEFT JOIN sys_store s ON b.store_id = s.id " +
            "WHERE 1=1 " +
            "<if test='recipeId != null'>AND b.recipe_id = #{recipeId}</if>" +
            "<if test='storeId != null'>AND b.store_id = #{storeId}</if>" +
            "<if test='batchNo != null'>AND b.batch_no LIKE CONCAT('%', #{batchNo}, '%')</if>" +
            "<if test='warningType != null and warningType == 1'>AND b.expire_time &lt; NOW()</if>" +
            "<if test='warningType != null and warningType == 2'>AND b.expire_time &gt; NOW() AND b.expire_time &lt; DATE_ADD(NOW(), INTERVAL r.warning_hours HOUR)</if>" +
            "<if test='warningType != null and warningType == 3'>AND b.expire_time &gt;= DATE_ADD(NOW(), INTERVAL r.warning_hours HOUR)</if>" +
            "AND b.status IN (1,2) " +
            "ORDER BY b.expire_time ASC, b.produce_time DESC" +
            "</script>")
    IPage<ProdBatch> selectBatchPage(Page<ProdBatch> page,
                                      @Param("recipeId") Long recipeId,
                                      @Param("storeId") Long storeId,
                                      @Param("batchNo") String batchNo,
                                      @Param("warningType") Integer warningType);

    @Select("SELECT b.*, r.product_name, r.product_code, r.unit " +
            "FROM prod_batch b " +
            "LEFT JOIN base_recipe r ON b.recipe_id = r.id " +
            "WHERE b.recipe_id = #{recipeId} AND b.store_id = #{storeId} " +
            "AND b.status IN (1,2) AND b.expire_time > NOW() " +
            "ORDER BY b.expire_time ASC")
    List<ProdBatch> selectAvailableBatch(@Param("recipeId") Long recipeId, @Param("storeId") Long storeId);

    @Update("UPDATE prod_batch SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT COALESCE(b.produce_qty - COALESCE(SUM(CASE WHEN s.biz_type = 'TRANSFER_OUT' THEN s.qty_change ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN s.biz_type = 'DAMAGE' THEN s.qty_change ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN s.biz_type = 'SALE' THEN s.qty_change ELSE 0 END), 0), 0) " +
            "FROM prod_batch b " +
            "LEFT JOIN stock_log s ON s.batch_id = b.id AND s.biz_type IN ('TRANSFER_OUT','DAMAGE','SALE') " +
            "WHERE b.id = #{batchId}")
    BigDecimal getRemainQty(@Param("batchId") Long batchId);

    @Select("SELECT COUNT(*) FROM prod_batch b " +
            "LEFT JOIN base_recipe r ON b.recipe_id = r.id " +
            "WHERE b.status IN (1,2) AND b.store_id = #{storeId} " +
            "AND b.expire_time > NOW() AND b.expire_time &lt; DATE_ADD(NOW(), INTERVAL r.warning_hours HOUR)")
    Integer countExpiring(@Param("storeId") Long storeId);

    @Select("SELECT COUNT(*) FROM prod_batch b WHERE b.status IN (1,2) AND b.store_id = #{storeId} AND b.expire_time &lt; NOW()")
    Integer countExpired(@Param("storeId") Long storeId);

    @Select("<script>" +
            "SELECT b.*, r.product_name, r.unit, r.warning_hours " +
            "FROM prod_batch b " +
            "LEFT JOIN base_recipe r ON b.recipe_id = r.id " +
            "WHERE b.status IN (1,2) AND b.store_id = #{storeId} " +
            "AND b.expire_time &gt; NOW() AND b.expire_time &lt; DATE_ADD(NOW(), INTERVAL r.warning_hours HOUR) " +
            "ORDER BY b.expire_time ASC" +
            "</script>")
    List<ProdBatch> selectWarningList(@Param("storeId") Long storeId);
}

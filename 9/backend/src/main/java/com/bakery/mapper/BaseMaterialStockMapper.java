package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.BaseMaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BaseMaterialStockMapper extends BaseMapper<BaseMaterialStock> {

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM base_material_stock WHERE material_id = #{materialId} AND store_id = #{storeId} AND quantity > 0 AND expire_date > NOW()")
    BigDecimal getAvailableStock(@Param("materialId") Long materialId, @Param("storeId") Long storeId);

    @Select("SELECT * FROM base_material_stock WHERE material_id = #{materialId} AND store_id = #{storeId} AND quantity > 0 AND expire_date > NOW() ORDER BY expire_date ASC")
    List<BaseMaterialStock> selectAvailableStock(@Param("materialId") Long materialId, @Param("storeId") Long storeId);

    @Update("UPDATE base_material_stock SET quantity = quantity - #{qty} WHERE id = #{id}")
    int deductStock(@Param("id") Long id, @Param("qty") BigDecimal qty);
}

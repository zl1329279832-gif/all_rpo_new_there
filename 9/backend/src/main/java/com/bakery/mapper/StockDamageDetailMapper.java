package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.StockDamageDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockDamageDetailMapper extends BaseMapper<StockDamageDetail> {

    @Select("SELECT d.*, b.batch_no, r.product_name, b.expire_time " +
            "FROM stock_damage_detail d " +
            "LEFT JOIN prod_batch b ON d.batch_id = b.id " +
            "LEFT JOIN base_recipe r ON d.recipe_id = r.id " +
            "WHERE d.damage_id = #{damageId}")
    List<StockDamageDetail> selectByDamageId(@Param("damageId") Long damageId);
}

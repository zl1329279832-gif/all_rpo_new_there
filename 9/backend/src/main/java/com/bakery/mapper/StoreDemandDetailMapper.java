package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.StoreDemandDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreDemandDetailMapper extends BaseMapper<StoreDemandDetail> {

    @Select("SELECT d.*, r.product_name, r.product_code, r.unit " +
            "FROM store_demand_detail d " +
            "LEFT JOIN base_recipe r ON d.recipe_id = r.id " +
            "WHERE d.demand_id = #{demandId}")
    List<StoreDemandDetail> selectByDemandId(@Param("demandId") Long demandId);
}

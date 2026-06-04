package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.ProdPlanDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProdPlanDetailMapper extends BaseMapper<ProdPlanDetail> {

    @Select("SELECT d.*, r.product_name, r.product_code, r.unit " +
            "FROM prod_plan_detail d " +
            "LEFT JOIN base_recipe r ON d.recipe_id = r.id " +
            "WHERE d.plan_id = #{planId}")
    List<ProdPlanDetail> selectByPlanId(@Param("planId") Long planId);
}

package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.BaseRecipeDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BaseRecipeDetailMapper extends BaseMapper<BaseRecipeDetail> {

    @Select("SELECT * FROM base_recipe_detail WHERE recipe_id = #{recipeId}")
    List<BaseRecipeDetail> selectByRecipeId(@Param("recipeId") Long recipeId);
}

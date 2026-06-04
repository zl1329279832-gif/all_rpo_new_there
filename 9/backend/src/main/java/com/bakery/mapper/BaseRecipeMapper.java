package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.BaseRecipe;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaseRecipeMapper extends BaseMapper<BaseRecipe> {
}

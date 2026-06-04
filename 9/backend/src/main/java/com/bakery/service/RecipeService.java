package com.bakery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bakery.entity.BaseRecipe;
import com.bakery.entity.BaseRecipeDetail;
import com.bakery.mapper.BaseRecipeDetailMapper;
import com.bakery.mapper.BaseRecipeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeService extends ServiceImpl<BaseRecipeMapper, BaseRecipe> {

    @Autowired
    private BaseRecipeDetailMapper recipeDetailMapper;

    public List<BaseRecipe> list(String category, String keyword) {
        LambdaQueryWrapper<BaseRecipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseRecipe::getStatus, 1);
        if (category != null) {
            wrapper.eq(BaseRecipe::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(BaseRecipe::getProductName, keyword)
                    .or().like(BaseRecipe::getProductCode, keyword);
        }
        wrapper.orderByDesc(BaseRecipe::getCreateTime);
        return list(wrapper);
    }

    public Map<String, Object> getDetail(Long id) {
        BaseRecipe recipe = getById(id);
        List<BaseRecipeDetail> details = recipeDetailMapper.selectByRecipeId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("recipe", recipe);
        result.put("details", details);
        return result;
    }

    public Map<Long, List<BaseRecipeDetail>> getRecipeDetailsByRecipeIds(List<Long> recipeIds) {
        List<BaseRecipeDetail> allDetails = recipeDetailMapper.selectList(
                new LambdaQueryWrapper<BaseRecipeDetail>()
                        .in(BaseRecipeDetail::getRecipeId, recipeIds)
        );
        return allDetails.stream()
                .collect(Collectors.groupingBy(BaseRecipeDetail::getRecipeId));
    }
}

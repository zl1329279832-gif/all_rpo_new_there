package com.bakery.controller;

import com.bakery.common.Result;
import com.bakery.entity.BaseRecipe;
import com.bakery.service.RecipeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "配方管理")
@RestController
@RequestMapping("/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @ApiOperation("获取成品列表")
    @GetMapping("/list")
    public Result<List<BaseRecipe>> list(@RequestParam(required = false) String category,
                                       @RequestParam(required = false) String keyword) {
        return Result.success(recipeService.list(category, keyword));
    }

    @ApiOperation("获取配方详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        return Result.success(recipeService.getDetail(id));
    }

    @ApiOperation("新增配方")
    @PostMapping
    public Result<Void> save(@RequestBody BaseRecipe recipe) {
        recipeService.save(recipe);
        return Result.success();
    }

    @ApiOperation("更新配方")
    @PutMapping
    public Result<Void> updateById(@RequestBody BaseRecipe recipe) {
        recipeService.updateById(recipe);
        return Result.success();
    }

    @ApiOperation("删除配方")
    @DeleteMapping("/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        recipeService.removeById(id);
        return Result.success();
    }
}

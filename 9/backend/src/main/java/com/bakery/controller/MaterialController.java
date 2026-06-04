package com.bakery.controller;

import com.bakery.common.Result;
import com.bakery.entity.BaseMaterial;
import com.bakery.entity.BaseMaterialStock;
import com.bakery.service.MaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "原料管理")
@RestController
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @ApiOperation("获取原料列表")
    @GetMapping("/list")
    public Result<List<BaseMaterial>> list(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) String keyword) {
        return Result.success(materialService.list(category, keyword));
    }

    @ApiOperation("获取原料详情")
    @GetMapping("/{id}")
    public Result<BaseMaterial> getById(@PathVariable Long id) {
        return Result.success(materialService.getById(id));
    }

    @ApiOperation("获取原料库存列表")
    @GetMapping("/stock")
    public Result<List<BaseMaterialStock>> getStockList(@RequestParam Long materialId,
                                                       @RequestParam(defaultValue = "1") Long storeId) {
        return Result.success(materialService.getStockList(materialId, storeId));
    }

    @ApiOperation("新增原料")
    @PostMapping
    public Result<Void> save(@RequestBody BaseMaterial material) {
        materialService.save(material);
        return Result.success();
    }

    @ApiOperation("更新原料")
    @PutMapping
    public Result<Void> updateById(@RequestBody BaseMaterial material) {
        materialService.updateById(material);
        return Result.success();
    }

    @ApiOperation("删除原料")
    @DeleteMapping("/{id}")
    public Result<Void> removeById(@PathVariable Long id) {
        materialService.removeById(id);
        return Result.success();
    }
}

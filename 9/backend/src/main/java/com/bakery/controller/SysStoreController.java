package com.bakery.controller;

import com.bakery.common.Result;
import com.bakery.entity.SysStore;
import com.bakery.mapper.SysStoreMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "门店管理")
@RestController
@RequestMapping("/store")
public class SysStoreController {

    @Autowired
    private SysStoreMapper sysStoreMapper;

    @ApiOperation("获取门店列表")
    @GetMapping("/list")
    public Result<List<SysStore>> list() {
        return Result.success(sysStoreMapper.selectList(null));
    }

    @ApiOperation("获取门店详情")
    @GetMapping("/{id}")
    public Result<SysStore> getById(@PathVariable Long id) {
        return Result.success(sysStoreMapper.selectById(id));
    }

    @ApiOperation("新增门店")
    @PostMapping
    public Result<Void> save(@RequestBody SysStore store) {
        sysStoreMapper.insert(store);
        return Result.success();
    }

    @ApiOperation("更新门店")
    @PutMapping
    public Result<Void> updateById(@RequestBody SysStore store) {
        sysStoreMapper.updateById(store);
        return Result.success();
    }
}

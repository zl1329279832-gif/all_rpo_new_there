package com.wms.controller;

import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Warehouse;
import com.wms.service.WarehouseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "仓库管理接口")
@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * 仓库列表
     *
     * @param status 状态
     * @return 仓库列表
     */
    @ApiOperation("仓库列表")
    @GetMapping
    public Result<PageResult<Warehouse>> getWarehouseList(
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        PageResult<Warehouse> result = warehouseService.queryWarehouses(status);
        return Result.success(result);
    }

    /**
     * 仓库详情
     *
     * @param id 仓库ID
     * @return 仓库详情
     */
    @ApiOperation("仓库详情")
    @GetMapping("/{id}")
    public Result<Warehouse> getWarehouseById(@ApiParam("仓库ID") @PathVariable Long id) {
        Warehouse warehouse = warehouseService.getById(id);
        return Result.success(warehouse);
    }

    /**
     * 获取所有启用的仓库
     *
     * @return 仓库列表
     */
    @ApiOperation("获取所有启用的仓库")
    @GetMapping("/all")
    public Result<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> list = warehouseService.getAll();
        return Result.success(list);
    }
}

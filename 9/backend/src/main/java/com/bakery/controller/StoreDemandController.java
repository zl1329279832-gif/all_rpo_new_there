package com.bakery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bakery.common.Result;
import com.bakery.entity.StoreDemand;
import com.bakery.entity.StoreDemandDetail;
import com.bakery.service.StoreDemandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "门店需求管理")
@RestController
@RequestMapping("/store-demand")
public class StoreDemandController {

    @Autowired
    private StoreDemandService storeDemandService;

    @ApiOperation("获取需求列表")
    @GetMapping("/page")
    public Result<IPage<StoreDemand>> getDemandPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                      @RequestParam(required = false) Integer status) {
        return Result.success(storeDemandService.getDemandPage(pageNum, pageSize, status));
    }

    @ApiOperation("获取需求详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDemandDetail(@PathVariable Long id) {
        return Result.success(storeDemandService.getDemandDetail(id));
    }

    @ApiOperation("确认需求")
    @PutMapping("/confirm/{id}")
    public Result<Void> confirmDemand(@PathVariable Long id,
                                    @RequestBody List<StoreDemandDetail> details) {
        storeDemandService.confirmDemand(id, details);
        return Result.success("确认成功");
    }

    @ApiOperation("发货")
    @PutMapping("/deliver/{id}")
    public Result<Void> deliverDemand(@PathVariable Long id,
                                         @RequestBody List<StoreDemandDetail> details) {
        storeDemandService.deliverDemand(id, details);
        return Result.success("发货成功");
    }

    @ApiOperation("完成需求")
    @PutMapping("/complete/{id}")
    public Result<Void> completeDemand(@PathVariable Long id) {
        storeDemandService.completeDemand(id);
        return Result.success("已完成");
    }
}

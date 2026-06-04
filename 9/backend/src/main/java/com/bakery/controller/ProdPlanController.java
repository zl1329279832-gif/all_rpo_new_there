package com.bakery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bakery.common.Result;
import com.bakery.dto.ProdPlanDTO;
import com.bakery.dto.ProduceCompleteDTO;
import com.bakery.entity.ProdPlan;
import com.bakery.service.ProdPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Api(tags = "生产计划管理")
@RestController
@RequestMapping("/prod-plan")
public class ProdPlanController {

    @Autowired
    private ProdPlanService prodPlanService;

    @ApiOperation("获取生产计划列表")
    @GetMapping("/page")
    public Result<IPage<ProdPlan>> getPlanPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate planDate,
                                             @RequestParam(required = false) Integer status) {
        return Result.success(prodPlanService.getPlanPage(pageNum, pageSize, planDate, status));
    }

    @ApiOperation("获取生产计划详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getPlanDetail(@PathVariable Long id) {
        return Result.success(prodPlanService.getPlanDetail(id));
    }

    @ApiOperation("创建生产计划")
    @PostMapping
    public Result<Long> createPlan(@RequestBody ProdPlanDTO dto) {
        return Result.success("创建成功", prodPlanService.createPlan(dto));
    }

    @ApiOperation("审核生产计划")
    @PutMapping("/audit/{id}")
    public Result<Void> auditPlan(@PathVariable Long id) {
        prodPlanService.auditPlan(id);
        return Result.success("审核成功");
    }

    @ApiOperation("开始生产")
    @PutMapping("/start/{id}")
    public Result<Void> startProduce(@PathVariable Long id) {
        prodPlanService.startProduce(id);
        return Result.success("已开始生产");
    }

    @ApiOperation("完成生产")
    @PutMapping("/complete")
    public Result<Void> completeProduce(@RequestBody ProduceCompleteDTO dto) {
        prodPlanService.completeProduce(dto);
        return Result.success("生产完成");
    }

    @ApiOperation("取消生产计划")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelPlan(@PathVariable Long id) {
        ProdPlan plan = prodPlanService.getById(id);
        plan.setStatus(4);
        prodPlanService.updateById(plan);
        return Result.success("已取消");
    }
}

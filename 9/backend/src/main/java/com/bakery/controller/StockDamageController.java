package com.bakery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bakery.common.Result;
import com.bakery.dto.StockDamageDTO;
import com.bakery.entity.StockDamage;
import com.bakery.service.StockDamageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "报损管理")
@RestController
@RequestMapping("/stock-damage")
public class StockDamageController {

    @Autowired
    private StockDamageService stockDamageService;

    @ApiOperation("获取报损列表")
    @GetMapping("/page")
    public Result<IPage<StockDamage>> getDamagePage(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String damageNo,
                                               @RequestParam(required = false) Integer damageType,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(stockDamageService.getDamagePage(pageNum, pageSize, damageNo, damageType, status));
    }

    @ApiOperation("获取报损详情")
    @GetMapping("/{id}")
    public Result<StockDamage> getDamageDetail(@PathVariable Long id) {
        return Result.success(stockDamageService.getDamageDetail(id));
    }

    @ApiOperation("创建报损单")
    @PostMapping
    public Result<Long> createDamage(@RequestBody StockDamageDTO dto) {
        return Result.success("创建成功", stockDamageService.createDamage(dto));
    }

    @ApiOperation("审核报损单")
    @PutMapping("/audit/{id}")
    public Result<Void> auditDamage(@PathVariable Long id,
                               @RequestParam Integer status,
                               @RequestParam(required = false) String auditOpinion) {
        stockDamageService.auditDamage(id, status, auditOpinion);
        return Result.successMsg("审核完成");
    }
}

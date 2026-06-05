package com.bakery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bakery.common.Result;
import com.bakery.dto.AnalysisVO;
import com.bakery.entity.ProdBatch;
import com.bakery.service.ProdBatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "成品批次管理")
@RestController
@RequestMapping("/prod-batch")
public class ProdBatchController {

    @Autowired
    private ProdBatchService prodBatchService;

    @ApiOperation("获取批次列表")
    @GetMapping("/page")
    public Result<Map<String, Object>> getBatchPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) Long recipeId,
                                               @RequestParam(required = false) Long storeId,
                                               @RequestParam(required = false) String batchNo,
                                               @RequestParam(required = false) String productName,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) String warningStatus) {
        Integer warningType = null;
        if ("expired".equals(warningStatus)) {
            warningType = 1;
        } else if ("expiring".equals(warningStatus)) {
            warningType = 2;
        } else if ("normal".equals(warningStatus)) {
            warningType = 3;
        }
        IPage<ProdBatch> page = prodBatchService.getBatchPage(pageNum, pageSize, recipeId, storeId, batchNo, warningType);
        List<ProdBatch> records = page.getRecords();
        if (productName != null && !productName.isEmpty()) {
            records = records.stream().filter(b -> b.getProductName() != null && b.getProductName().contains(productName)).collect(java.util.stream.Collectors.toList());
        }
        if (status != null) {
            records = records.stream().filter(b -> status.equals(b.getStatus())).collect(java.util.stream.Collectors.toList());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", records.size());
        return Result.success(result);
    }

    @ApiOperation("获取批次详情")
    @GetMapping("/{id}")
    public Result<ProdBatch> getBatchDetail(@PathVariable Long id) {
        return Result.success(prodBatchService.getBatchDetail(id));
    }

    @ApiOperation("获取可用批次列表（效期优先）")
    @GetMapping("/available")
    public Result<List<ProdBatch>> getAvailableBatches(@RequestParam Long recipeId,
                                                      @RequestParam(defaultValue = "1") Long storeId) {
        return Result.success(prodBatchService.getAvailableBatches(recipeId, storeId));
    }

    @ApiOperation("获取批次剩余数量")
    @GetMapping("/remain/{batchId}")
    public Result<BigDecimal> getRemainQty(@PathVariable Long batchId) {
        return Result.success(prodBatchService.getRemainQty(batchId));
    }

    @ApiOperation("获取临期预警统计")
    @GetMapping("/warning/stats")
    public Result<AnalysisVO.WarningStatsVO> getWarningStats(@RequestParam(defaultValue = "1") Long storeId) {
        return Result.success(prodBatchService.getWarningStats(storeId));
    }

    @ApiOperation("获取临期预警列表（分页）")
    @GetMapping("/warning/list")
    public Result<Map<String, Object>> getWarningList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Long storeId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String warningLevel) {
        IPage<ProdBatch> page = prodBatchService.getWarningPage(pageNum, pageSize, storeId, productName, warningLevel);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    @ApiOperation("库存效期总览（按产品分组）")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getProductOverview(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Long storeId,
            @RequestParam(required = false) String productName) {
        IPage<Map<String, Object>> page = prodBatchService.getProductOverview(pageNum, pageSize, storeId, productName);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        return Result.success(result);
    }
}

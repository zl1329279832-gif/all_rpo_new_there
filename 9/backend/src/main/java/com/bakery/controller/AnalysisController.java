package com.bakery.controller;

import com.bakery.common.Result;
import com.bakery.dto.AnalysisVO;
import com.bakery.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Api(tags = "经营分析")
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @ApiOperation("获取经营分析数据")
    @GetMapping
    public Result<AnalysisVO> getAnalysisData(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long storeId) {
        if (days != null && startDate == null) {
            startDate = LocalDate.now().minusDays(days);
            endDate = LocalDate.now();
        }
        return Result.success(analysisService.getAnalysisData(startDate, endDate, storeId));
    }

    @ApiOperation("刷新缓存")
    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        analysisService.clearAnalysisCache();
        return Result.successMsg("缓存已刷新");
    }
}

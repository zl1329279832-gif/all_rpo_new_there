package com.bakery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bakery.common.Result;
import com.bakery.dto.StockTransferDTO;
import com.bakery.entity.StockTransfer;
import com.bakery.service.StockTransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "门店调拨管理")
@RestController
@RequestMapping("/stock-transfer")
public class StockTransferController {

    @Autowired
    private StockTransferService stockTransferService;

    @ApiOperation("获取调拨列表")
    @GetMapping("/page")
    public Result<Map<String, Object>> getTransferPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String transferNo,
                                                    @RequestParam(required = false) Integer transferType,
                                                    @RequestParam(required = false) Integer status) {
        IPage<StockTransfer> page = stockTransferService.getTransferPage(pageNum, pageSize, transferNo, transferType, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    @ApiOperation("获取调拨详情")
    @GetMapping("/{id}")
    public Result<StockTransfer> getTransferDetail(@PathVariable Long id) {
        return Result.success(stockTransferService.getTransferDetail(id));
    }

    @ApiOperation("创建调拨单")
    @PostMapping
    public Result<Long> createTransfer(@RequestBody StockTransferDTO dto) {
        return Result.success("创建成功", stockTransferService.createTransfer(dto));
    }

    @ApiOperation("确认出库")
    @PutMapping("/outbound/{id}")
    public Result<Void> confirmOutbound(@PathVariable Long id) {
        stockTransferService.confirmOutbound(id);
        return Result.successMsg("出库成功");
    }

    @ApiOperation("确认入库")
    @PutMapping("/inbound/{id}")
    public Result<Void> confirmInbound(@PathVariable Long id) {
        stockTransferService.confirmInbound(id);
        return Result.successMsg("入库成功");
    }

    @ApiOperation("取消调拨")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelTransfer(@PathVariable Long id) {
        StockTransfer transfer = stockTransferService.getById(id);
        transfer.setStatus(3);
        stockTransferService.updateById(transfer);
        return Result.successMsg("已取消");
    }
}

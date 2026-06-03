package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.AlertHandleDTO;
import com.wms.entity.InventoryAlert;
import com.wms.service.InventoryAlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "库存预警接口")
@RestController
@RequestMapping("/alert")
public class InventoryAlertController {

    @Autowired
    private InventoryAlertService inventoryAlertService;

    /**
     * 预警列表
     *
     * @param query      分页参数
     * @param alertType  预警类型
     * @param alertLevel 预警级别
     * @param status     状态
     * @param warehouseId 仓库ID
     * @param productId  商品ID
     * @return 预警列表
     */
    @ApiOperation("预警列表")
    @GetMapping
    public Result<PageResult<InventoryAlert>> getAlertList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("预警类型") @RequestParam(required = false) Integer alertType,
            @ApiParam("预警级别") @RequestParam(required = false) Integer alertLevel,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("商品ID") @RequestParam(required = false) Long productId) {
        PageResult<InventoryAlert> result = inventoryAlertService.queryAlerts(
                query, alertType, alertLevel, status, warehouseId, productId);
        return Result.success(result);
    }

    /**
     * 检查预警（临期、过期、库存上下限）
     *
     * @return 操作结果
     */
    @ApiOperation("检查预警（临期、过期、库存上下限）")
    @PostMapping("/check")
    public Result<Void> checkAlert() {
        inventoryAlertService.checkExpireAlert();
        inventoryAlertService.checkStockThresholdAlert();
        return Result.success();
    }

    /**
     * 标记预警已处理
     *
     * @param id       预警ID
     * @param dto      处理信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("标记预警已处理")
    @PostMapping("/{id}/handle")
    public Result<Void> handleAlert(
            @ApiParam("预警ID") @PathVariable Long id,
            @ApiParam("处理信息") @Validated @RequestBody AlertHandleDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        inventoryAlertService.handleAlert(id, dto.getHandleResult(),
                operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 预警看板数据
     *
     * @return 预警看板数据
     */
    @ApiOperation("预警看板数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getAlertDashboard() {
        int pendingCount = inventoryAlertService.countPendingAlerts();
        Map<String, Object> result = new HashMap<>();
        result.put("pendingCount", pendingCount);
        return Result.success(result);
    }
}

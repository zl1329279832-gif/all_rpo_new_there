package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.StocktakeDiffProcessDTO;
import com.wms.dto.StocktakeResultDTO;
import com.wms.entity.StocktakeOrder;
import com.wms.entity.StocktakeOrderDetail;
import com.wms.service.StocktakeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "盘点管理接口")
@RestController
@RequestMapping("/stocktake")
public class StocktakeController {

    @Autowired
    private StocktakeService stocktakeService;

    /**
     * 盘点单列表
     *
     * @param query          分页参数
     * @param stocktakeType  盘点类型
     * @param status         状态
     * @param warehouseId    仓库ID
     * @param areaId         库区ID
     * @param handler        盘点人
     * @return 盘点单列表
     */
    @ApiOperation("盘点单列表")
    @GetMapping
    public Result<PageResult<StocktakeOrder>> getStocktakeList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("盘点类型") @RequestParam(required = false) Integer stocktakeType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("库区ID") @RequestParam(required = false) Long areaId,
            @ApiParam("盘点人") @RequestParam(required = false) String handler) {
        PageResult<StocktakeOrder> result = stocktakeService.queryStocktakeOrders(
                query, stocktakeType, status, warehouseId, areaId, handler);
        return Result.success(result);
    }

    /**
     * 盘点单详情
     *
     * @param id 盘点单ID
     * @return 盘点单详情（包含明细）
     */
    @ApiOperation("盘点单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getStocktakeDetail(@ApiParam("盘点单ID") @PathVariable Long id) {
        StocktakeOrder order = stocktakeService.getById(id);
        List<StocktakeOrderDetail> details = stocktakeService.getDetailsByOrderId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 创建盘点单
     *
     * @param order    盘点单信息
     * @param operator 操作人
     * @return 盘点单ID
     */
    @ApiOperation("创建盘点单")
    @PostMapping
    public Result<Long> createStocktakeOrder(
            @ApiParam("盘点单信息") @Validated @RequestBody StocktakeOrder order,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = stocktakeService.createStocktakeOrder(order, operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 录入盘点结果
     *
     * @param id       盘点单ID
     * @param dto      盘点结果信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("录入盘点结果")
    @PostMapping("/{id}/result")
    public Result<Void> enterStocktakeResult(
            @ApiParam("盘点单ID") @PathVariable Long id,
            @ApiParam("盘点结果信息") @Validated @RequestBody StocktakeResultDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        stocktakeService.enterStocktakeResult(dto, operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 盘点确认（处理差异）
     *
     * @param id       盘点单ID
     * @param dto      差异处理信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("盘点确认（处理差异）")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmStocktakeComplete(
            @ApiParam("盘点单ID") @PathVariable Long id,
            @ApiParam("差异处理信息") @Validated @RequestBody StocktakeDiffProcessDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        stocktakeService.processDiff(dto.getDetailId(), dto.getProcessStatus(),
                dto.getProcessResult(), operator != null ? operator : "system");
        stocktakeService.confirmStocktakeComplete(id, operator != null ? operator : "system");
        return Result.success();
    }
}

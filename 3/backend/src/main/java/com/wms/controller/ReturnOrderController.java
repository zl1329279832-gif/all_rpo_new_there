package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.ReturnConfirmDTO;
import com.wms.dto.ReturnInspectDTO;
import com.wms.entity.ReturnOrder;
import com.wms.entity.ReturnOrderDetail;
import com.wms.service.ReturnOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "退货管理接口")
@RestController
@RequestMapping("/return")
public class ReturnOrderController {

    @Autowired
    private ReturnOrderService returnOrderService;

    /**
     * 退货单列表
     *
     * @param query              分页参数
     * @param returnType         退货类型
     * @param status             状态
     * @param warehouseId        仓库ID
     * @param originalShipmentNo 原出库单号
     * @return 退货单列表
     */
    @ApiOperation("退货单列表")
    @GetMapping
    public Result<PageResult<ReturnOrder>> getReturnList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("退货类型") @RequestParam(required = false) Integer returnType,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("原出库单号") @RequestParam(required = false) String originalShipmentNo) {
        PageResult<ReturnOrder> result = returnOrderService.queryReturnOrders(
                query, returnType, status, warehouseId, originalShipmentNo);
        return Result.success(result);
    }

    /**
     * 退货单详情
     *
     * @param id 退货单ID
     * @return 退货单详情（包含明细）
     */
    @ApiOperation("退货单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getReturnDetail(@ApiParam("退货单ID") @PathVariable Long id) {
        ReturnOrder order = returnOrderService.getById(id);
        List<ReturnOrderDetail> details = returnOrderService.getDetailsByOrderId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 创建退货单
     *
     * @param order    退货单信息
     * @param details  退货明细
     * @param operator 操作人
     * @return 退货单ID
     */
    @ApiOperation("创建退货单")
    @PostMapping
    public Result<Long> createReturnOrder(
            @ApiParam("退货单信息") @Validated @RequestBody ReturnOrder order,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = returnOrderService.createReturnOrder(order, order.getDetails(), operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 退货质检
     *
     * @param id       退货单ID
     * @param dto      质检信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("退货质检")
    @PostMapping("/{id}/inspect")
    public Result<Void> doReturnInspection(
            @ApiParam("退货单ID") @PathVariable Long id,
            @ApiParam("质检信息") @Validated @RequestBody ReturnInspectDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        returnOrderService.doReturnInspection(dto.getDetailId(), dto.getInspectionResult(),
                dto.getActualQuantity(), operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 退货入库确认
     *
     * @param id       退货单ID
     * @param dto      入库确认信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("退货入库确认")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReturnComplete(
            @ApiParam("退货单ID") @PathVariable Long id,
            @ApiParam("入库确认信息") @Validated @RequestBody ReturnConfirmDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        returnOrderService.returnToStock(dto.getDetailId(), dto.getLocationId(),
                operator != null ? operator : "system");
        returnOrderService.confirmReturnComplete(id, operator != null ? operator : "system");
        return Result.success();
    }
}

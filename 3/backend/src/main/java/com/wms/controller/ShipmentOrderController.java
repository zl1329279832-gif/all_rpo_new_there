package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.ShipmentCancelDTO;
import com.wms.dto.ShipmentOrderCreateDTO;
import com.wms.entity.ShipmentAllocateDetail;
import com.wms.entity.ShipmentOrder;
import com.wms.entity.ShipmentOrderDetail;
import com.wms.service.ShipmentOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "出库管理接口")
@RestController
@RequestMapping("/api/shipment")
public class ShipmentOrderController {

    @Autowired
    private ShipmentOrderService shipmentOrderService;

    /**
     * 出库单列表
     *
     * @param query        分页参数
     * @param shipmentType 出库类型
     * @param orderStatus  订单状态
     * @param warehouseId  仓库ID
     * @param customerName 客户名称
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 出库单列表
     */
    @ApiOperation("出库单列表")
    @GetMapping
    public Result<PageResult<ShipmentOrder>> getShipmentList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("出库类型") @RequestParam(required = false) Integer shipmentType,
            @ApiParam("订单状态") @RequestParam(required = false) Integer orderStatus,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("客户名称") @RequestParam(required = false) String customerName,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        PageResult<ShipmentOrder> result = shipmentOrderService.queryShipmentOrders(
                query, shipmentType, orderStatus, warehouseId, customerName, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 出库单详情
     *
     * @param id 出库单ID
     * @return 出库单详情（包含明细和分配明细）
     */
    @ApiOperation("出库单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getShipmentDetail(@ApiParam("出库单ID") @PathVariable Long id) {
        ShipmentOrder order = shipmentOrderService.getById(id);
        List<ShipmentOrderDetail> details = shipmentOrderService.getDetailsByOrderId(id);
        List<ShipmentAllocateDetail> allocateDetails = shipmentOrderService.getAllocateDetailsByOrderId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);
        result.put("allocateDetails", allocateDetails);
        return Result.success(result);
    }

    /**
     * 创建出库单
     *
     * @param dto      出库单信息
     * @param operator 操作人
     * @return 出库单ID
     */
    @ApiOperation("创建出库单")
    @PostMapping
    public Result<Long> createShipmentOrder(
            @ApiParam("出库单信息") @Validated @RequestBody ShipmentOrderCreateDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = shipmentOrderService.createShipmentOrder(dto, operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 库存分配
     *
     * @param id       出库单ID
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("库存分配")
    @PostMapping("/{id}/allocate")
    public Result<Void> allocateInventory(
            @ApiParam("出库单ID") @PathVariable Long id,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        shipmentOrderService.allocateInventory(id, operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 出库撤销
     *
     * @param id       出库单ID
     * @param dto      撤销信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("出库撤销")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelShipment(
            @ApiParam("出库单ID") @PathVariable Long id,
            @ApiParam("撤销信息") @Validated @RequestBody ShipmentCancelDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        shipmentOrderService.cancelShipment(id, dto.getCancelReason(), operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 出库完成确认
     *
     * @param id       出库单ID
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("出库完成确认")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmShipmentComplete(
            @ApiParam("出库单ID") @PathVariable Long id,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        shipmentOrderService.confirmShipmentComplete(id, operator != null ? operator : "system");
        return Result.success();
    }
}

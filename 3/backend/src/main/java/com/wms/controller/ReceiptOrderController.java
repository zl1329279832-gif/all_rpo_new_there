package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.AssignLocationDTO;
import com.wms.dto.ReceiptArrivalDTO;
import com.wms.dto.ReceiptInspectDTO;
import com.wms.dto.ReceiptOrderCreateDTO;
import com.wms.entity.ReceiptOrder;
import com.wms.entity.ReceiptOrderDetail;
import com.wms.service.ReceiptOrderService;
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

@Api(tags = "入库管理接口")
@RestController
@RequestMapping("/receipt")
public class ReceiptOrderController {

    @Autowired
    private ReceiptOrderService receiptOrderService;

    /**
     * 入库单列表
     *
     * @param query       分页参数
     * @param receiptType 入库类型
     * @param orderStatus 订单状态
     * @param warehouseId 仓库ID
     * @param supplierId  供应商ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 入库单列表
     */
    @ApiOperation("入库单列表")
    @GetMapping
    public Result<PageResult<ReceiptOrder>> getReceiptList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("入库类型") @RequestParam(required = false) Integer receiptType,
            @ApiParam("订单状态") @RequestParam(required = false) Integer orderStatus,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("供应商ID") @RequestParam(required = false) Long supplierId,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        PageResult<ReceiptOrder> result = receiptOrderService.queryReceiptOrders(
                query, receiptType, orderStatus, warehouseId, supplierId, startTime, endTime);
        return Result.success(result);
    }

    /**
     * 入库单详情
     *
     * @param id 入库单ID
     * @return 入库单详情（包含明细）
     */
    @ApiOperation("入库单详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getReceiptDetail(@ApiParam("入库单ID") @PathVariable Long id) {
        ReceiptOrder order = receiptOrderService.getById(id);
        List<ReceiptOrderDetail> details = receiptOrderService.getDetailsByOrderId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 创建入库单
     *
     * @param dto      入库单信息
     * @param operator 操作人
     * @return 入库单ID
     */
    @ApiOperation("创建入库单")
    @PostMapping
    public Result<Long> createReceiptOrder(
            @ApiParam("入库单信息") @Validated @RequestBody ReceiptOrderCreateDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = receiptOrderService.createReceiptOrder(dto, operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 到货确认
     *
     * @param id       入库单ID
     * @param dto      到货信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("到货确认")
    @PostMapping("/{id}/arrival")
    public Result<Void> confirmArrival(
            @ApiParam("入库单ID") @PathVariable Long id,
            @ApiParam("到货信息") @Validated @RequestBody ReceiptArrivalDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Date arrivalTime = dto.getArrivalTime() != null ? dto.getArrivalTime() : new Date();
        receiptOrderService.confirmArrival(id, arrivalTime, operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 入库质检
     *
     * @param id       入库单ID
     * @param dto      质检信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("入库质检")
    @PostMapping("/{id}/inspect")
    public Result<Void> doInspection(
            @ApiParam("入库单ID") @PathVariable Long id,
            @ApiParam("质检信息") @Validated @RequestBody ReceiptInspectDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        receiptOrderService.doInspection(dto.getDetailId(), dto.getArrivalQuantity(),
                dto.getQualifiedQuantity(), dto.getUnqualifiedQuantity(),
                dto.getInspectionResult(), dto.getInspectionRemark(),
                operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 库位分配上架
     *
     * @param id       入库单ID
     * @param dto      库位分配信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("库位分配上架")
    @PostMapping("/{id}/putaway")
    public Result<Void> assignLocation(
            @ApiParam("入库单ID") @PathVariable Long id,
            @ApiParam("库位分配信息") @Validated @RequestBody AssignLocationDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        receiptOrderService.assignLocation(dto.getDetailId(), dto.getLocationId(),
                operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 入库完成确认
     *
     * @param id       入库单ID
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("入库完成确认")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReceiptComplete(
            @ApiParam("入库单ID") @PathVariable Long id,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        receiptOrderService.confirmReceiptComplete(id, operator != null ? operator : "system");
        return Result.success();
    }
}

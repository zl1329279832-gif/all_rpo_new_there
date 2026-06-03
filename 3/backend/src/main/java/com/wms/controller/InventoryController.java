package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.InventoryFreezeDTO;
import com.wms.dto.InventoryQueryDTO;
import com.wms.entity.InventoryBatch;
import com.wms.entity.InventoryLog;
import com.wms.service.InventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "库存管理接口")
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 批次库存列表（分页、多条件）
     *
     * @param query 查询条件
     * @return 批次库存列表
     */
    @ApiOperation("批次库存列表")
    @GetMapping("/batch")
    public Result<PageResult<InventoryBatch>> getBatchList(@ApiParam("查询条件") @Validated InventoryQueryDTO query) {
        PageResult<InventoryBatch> result = inventoryService.queryInventoryBatch(query);
        return Result.success(result);
    }

    /**
     * 查询单个批次
     *
     * @param id 批次ID
     * @return 批次详情
     */
    @ApiOperation("查询单个批次")
    @GetMapping("/batch/{id}")
    public Result<InventoryBatch> getBatchById(@ApiParam("批次ID") @PathVariable Long id) {
        InventoryBatch batch = inventoryService.getById(id);
        return Result.success(batch);
    }

    /**
     * 批次追踪
     *
     * @param batchNo     批次号
     * @param warehouseId 仓库ID
     * @param productId   商品ID
     * @return 库存流水列表
     */
    @ApiOperation("批次追踪")
    @GetMapping("/batch/trace/{batchNo}")
    public Result<List<InventoryLog>> traceBatch(@ApiParam("批次号") @PathVariable String batchNo,
                                                 @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
                                                 @ApiParam("商品ID") @RequestParam(required = false) Long productId) {
        List<InventoryLog> list = inventoryService.queryBatchTrace(batchNo, warehouseId, productId);
        return Result.success(list);
    }

    /**
     * 库存冻结
     *
     * @param dto      冻结信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("库存冻结")
    @PostMapping("/freeze")
    public Result<Void> freezeInventory(@ApiParam("冻结信息") @Validated @RequestBody InventoryFreezeDTO dto,
                                        @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        inventoryService.freezeInventory(dto.getBatchId(), dto.getQuantity(),
                dto.getBusinessNo(), operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 库存解冻
     *
     * @param dto      解冻信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("库存解冻")
    @PostMapping("/unfreeze")
    public Result<Void> unfreezeInventory(@ApiParam("解冻信息") @Validated @RequestBody InventoryFreezeDTO dto,
                                          @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        inventoryService.unfreezeInventory(dto.getBatchId(), dto.getQuantity(),
                dto.getBusinessNo(), operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 库存流水列表
     *
     * @param query        分页参数
     * @param warehouseId  仓库ID
     * @param productId    商品ID
     * @param batchNo      批次号
     * @param businessType 业务类型
     * @param businessNo   业务单据号
     * @return 库存流水列表
     */
    @ApiOperation("库存流水列表")
    @GetMapping("/log")
    public Result<PageResult<InventoryLog>> getInventoryLogList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("商品ID") @RequestParam(required = false) Long productId,
            @ApiParam("批次号") @RequestParam(required = false) String batchNo,
            @ApiParam("业务类型") @RequestParam(required = false) Integer businessType,
            @ApiParam("业务单据号") @RequestParam(required = false) String businessNo) {
        PageResult<InventoryLog> result = inventoryService.queryInventoryLog(
                query, warehouseId, productId, batchNo, businessType, businessNo);
        return Result.success(result);
    }
}

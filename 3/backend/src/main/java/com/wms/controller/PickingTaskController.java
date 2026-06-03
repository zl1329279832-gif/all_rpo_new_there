package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.dto.GeneratePickingDTO;
import com.wms.dto.PickingConfirmDTO;
import com.wms.entity.PickingTask;
import com.wms.entity.PickingTaskDetail;
import com.wms.service.PickingTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "拣货任务接口")
@RestController
@RequestMapping("/picking")
public class PickingTaskController {

    @Autowired
    private PickingTaskService pickingTaskService;

    /**
     * 拣货任务列表
     *
     * @param query           分页参数
     * @param warehouseId     仓库ID
     * @param shipmentOrderId 出库单ID
     * @param picker          拣货员
     * @param status          状态
     * @param priority        优先级
     * @return 拣货任务列表
     */
    @ApiOperation("拣货任务列表")
    @GetMapping
    public Result<PageResult<PickingTask>> getPickingList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("仓库ID") @RequestParam(required = false) Long warehouseId,
            @ApiParam("出库单ID") @RequestParam(required = false) Long shipmentOrderId,
            @ApiParam("拣货员") @RequestParam(required = false) String picker,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("优先级") @RequestParam(required = false) Integer priority) {
        PageResult<PickingTask> result = pickingTaskService.queryPickingTasks(
                query, warehouseId, shipmentOrderId, picker, status, priority);
        return Result.success(result);
    }

    /**
     * 拣货任务详情
     *
     * @param id 拣货任务ID
     * @return 拣货任务详情（包含明细）
     */
    @ApiOperation("拣货任务详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getPickingDetail(@ApiParam("拣货任务ID") @PathVariable Long id) {
        PickingTask task = pickingTaskService.getById(id);
        List<PickingTaskDetail> details = pickingTaskService.getDetailsByTaskId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("details", details);
        return Result.success(result);
    }

    /**
     * 生成拣货任务
     *
     * @param dto      生成拣货任务信息
     * @param operator 操作人
     * @return 拣货任务ID列表
     */
    @ApiOperation("生成拣货任务")
    @PostMapping("/generate")
    public Result<List<Long>> generatePickingTasks(
            @ApiParam("生成拣货任务信息") @Validated @RequestBody GeneratePickingDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        List<Long> taskIds = pickingTaskService.generatePickingTasks(
                dto.getShipmentOrderIds(), dto.getPickingMode(),
                dto.getPicker(), dto.getPriority(),
                operator != null ? operator : "system");
        return Result.success(taskIds);
    }

    /**
     * 拣货确认
     *
     * @param id       拣货任务ID
     * @param dto      拣货确认信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("拣货确认")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmPicking(
            @ApiParam("拣货任务ID") @PathVariable Long id,
            @ApiParam("拣货确认信息") @Validated @RequestBody PickingConfirmDTO dto,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        pickingTaskService.confirmPicking(dto, operator != null ? operator : "system");
        return Result.success();
    }

    /**
     * 拣货完成
     *
     * @param id       拣货任务ID
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("拣货完成")
    @PostMapping("/{id}/complete")
    public Result<Void> completePicking(
            @ApiParam("拣货任务ID") @PathVariable Long id,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        pickingTaskService.completePicking(id, operator != null ? operator : "system");
        return Result.success();
    }
}

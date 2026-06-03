package com.medical.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.QcPlan;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.QcPlanMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "质控计划管理", description = "质控计划的增删改查及分页查询")
@RestController
@RequestMapping("/qc-plans")
@RequiredArgsConstructor
public class QcPlanController {

    private final QcPlanMapper qcPlanMapper;

    @Operation(summary = "分页查询质控计划列表", description = "根据条件分页查询质控计划")
    @GetMapping
    public Result<PageResult<QcPlan>> listQcPlans(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数，默认10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "关键词搜索（计划名称）") @RequestParam(required = false) String keyword,
            @Parameter(description = "质控类型") @RequestParam(required = false) Integer qcType,
            @Parameter(description = "周期类型") @RequestParam(required = false) Integer cycleType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId) {
        Page<QcPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<QcPlan> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(QcPlan::getPlanName, keyword);
        }
        if (qcType != null) {
            wrapper.eq(QcPlan::getQcType, qcType);
        }
        if (cycleType != null) {
            wrapper.eq(QcPlan::getCycleType, cycleType);
        }
        if (status != null) {
            wrapper.eq(QcPlan::getStatus, status);
        }
        if (deviceId != null) {
            wrapper.eq(QcPlan::getDeviceId, deviceId);
        }

        wrapper.orderByDesc(QcPlan::getId);
        IPage<QcPlan> result = qcPlanMapper.selectPage(page, wrapper);

        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize));
    }

    @Operation(summary = "获取质控计划详情", description = "根据ID获取质控计划详细信息")
    @GetMapping("/{id}")
    public Result<QcPlan> getQcPlan(@Parameter(description = "质控计划ID") @PathVariable Long id) {
        QcPlan qcPlan = qcPlanMapper.selectById(id);
        if (qcPlan == null) {
            throw new BusinessException("质控计划不存在");
        }
        return Result.success(qcPlan);
    }

    @Operation(summary = "创建质控计划", description = "创建新的质控计划")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createQcPlan(@RequestBody QcPlan qcPlan) {
        if (qcPlan.getStatus() == null) {
            qcPlan.setStatus(1);
        }
        qcPlanMapper.insert(qcPlan);
        return Result.success("创建质控计划成功");
    }

    @Operation(summary = "更新质控计划", description = "更新已有的质控计划信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateQcPlan(
            @Parameter(description = "质控计划ID") @PathVariable Long id,
            @RequestBody QcPlan qcPlan) {
        QcPlan existing = qcPlanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("质控计划不存在");
        }
        qcPlan.setId(id);
        qcPlanMapper.updateById(qcPlan);
        return Result.success("更新质控计划成功");
    }

    @Operation(summary = "删除质控计划", description = "根据ID删除质控计划（逻辑删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteQcPlan(@Parameter(description = "质控计划ID") @PathVariable Long id) {
        QcPlan qcPlan = qcPlanMapper.selectById(id);
        if (qcPlan == null) {
            throw new BusinessException("质控计划不存在");
        }
        qcPlanMapper.deleteById(id);
        return Result.success("删除质控计划成功");
    }

    @Operation(summary = "批量删除质控计划", description = "根据ID列表批量删除质控计划（逻辑删除）")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDeleteQcPlans(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的质控计划");
        }
        qcPlanMapper.deleteBatchIds(ids);
        return Result.success("批量删除质控计划成功");
    }

    @Operation(summary = "更新质控计划状态", description = "更新质控计划的启用/停用状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'QC_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateQcPlanStatus(
            @Parameter(description = "质控计划ID") @PathVariable Long id,
            @Parameter(description = "状态值") @RequestParam Integer status) {
        QcPlan qcPlan = qcPlanMapper.selectById(id);
        if (qcPlan == null) {
            throw new BusinessException("质控计划不存在");
        }
        qcPlan.setStatus(status);
        qcPlanMapper.updateById(qcPlan);
        return Result.success("更新质控计划状态成功");
    }

    @Operation(summary = "获取设备的质控计划列表", description = "根据设备ID获取关联的所有质控计划")
    @GetMapping("/device/{deviceId}")
    public Result<List<QcPlan>> getQcPlansByDeviceId(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        LambdaQueryWrapper<QcPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QcPlan::getDeviceId, deviceId);
        wrapper.orderByDesc(QcPlan::getId);
        List<QcPlan> list = qcPlanMapper.selectList(wrapper);
        return Result.success(list);
    }
}

package com.medical.device.controller;

import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.SparePart;
import com.medical.device.service.SparePartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "配件管理", description = "配件库存管理接口")
@RestController
@RequestMapping("/spare-parts")
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartService sparePartService;

    @Operation(summary = "分页查询配件列表")
    @GetMapping
    public Result<PageResult<SparePart>> listParts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "关键词（名称/编号/型号/厂商）") @RequestParam(required = false) String keyword,
            @Parameter(description = "库存上限筛选") @RequestParam(required = false) Integer minStock,
            @Parameter(description = "厂商ID") @RequestParam(required = false) Long manufacturer) {
        PageResult<SparePart> result = sparePartService.listParts(
                pageNum, pageSize, keyword, minStock, manufacturer);
        return Result.success(result);
    }

    @Operation(summary = "获取配件详情")
    @GetMapping("/{id}")
    public Result<SparePart> getPart(@Parameter(description = "配件ID") @PathVariable Long id) {
        SparePart part = sparePartService.getPart(id);
        return Result.success(part);
    }

    @Operation(summary = "新增配件")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<SparePart> createPart(@RequestBody SparePart part) {
        SparePart created = sparePartService.createPart(part);
        return Result.success(created);
    }

    @Operation(summary = "修改配件")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<SparePart> updatePart(@RequestBody SparePart part) {
        SparePart updated = sparePartService.updatePart(part);
        return Result.success(updated);
    }

    @Operation(summary = "删除配件")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<String> deletePart(@Parameter(description = "配件ID") @PathVariable Long id) {
        sparePartService.deletePart(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "配件入库")
    @PutMapping("/{id}/stock-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN', 'ENGINEER')")
    public Result<String> stockIn(
            @Parameter(description = "配件ID") @PathVariable Long id,
            @Parameter(description = "入库数量") @RequestParam Integer quantity) {
        sparePartService.stockIn(id, quantity);
        return Result.success("入库成功");
    }

    @Operation(summary = "配件出库")
    @PutMapping("/{id}/stock-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN', 'ENGINEER')")
    public Result<String> stockOut(
            @Parameter(description = "配件ID") @PathVariable Long id,
            @Parameter(description = "出库数量") @RequestParam Integer quantity) {
        sparePartService.stockOut(id, quantity);
        return Result.success("出库成功");
    }

    @Operation(summary = "查询库存预警配件")
    @GetMapping("/low-stock")
    public Result<List<SparePart>> getLowStockParts() {
        List<SparePart> parts = sparePartService.getLowStockParts();
        return Result.success(parts);
    }
}

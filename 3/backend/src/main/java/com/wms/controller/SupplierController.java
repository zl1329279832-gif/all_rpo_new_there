package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Supplier;
import com.wms.service.SupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "供应商管理接口")
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 供应商列表
     *
     * @param query        分页参数
     * @param supplierCode 供应商编码
     * @param supplierName 供应商名称
     * @param creditLevel  信用等级
     * @param status       状态
     * @return 供应商列表
     */
    @ApiOperation("供应商列表")
    @GetMapping
    public Result<PageResult<Supplier>> getSupplierList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("供应商编码") @RequestParam(required = false) String supplierCode,
            @ApiParam("供应商名称") @RequestParam(required = false) String supplierName,
            @ApiParam("信用等级") @RequestParam(required = false) Integer creditLevel,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        PageResult<Supplier> result = supplierService.querySuppliers(
                query, supplierCode, supplierName, creditLevel, status);
        return Result.success(result);
    }

    /**
     * 供应商详情
     *
     * @param id 供应商ID
     * @return 供应商详情
     */
    @ApiOperation("供应商详情")
    @GetMapping("/{id}")
    public Result<Supplier> getSupplierById(@ApiParam("供应商ID") @PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        return Result.success(supplier);
    }

    /**
     * 新增供应商
     *
     * @param supplier 供应商信息
     * @param operator 操作人
     * @return 供应商ID
     */
    @ApiOperation("新增供应商")
    @PostMapping
    public Result<Long> createSupplier(
            @ApiParam("供应商信息") @Validated @RequestBody Supplier supplier,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = supplierService.createSupplier(supplier, operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 更新供应商
     *
     * @param supplier 供应商信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("更新供应商")
    @PutMapping
    public Result<Void> updateSupplier(
            @ApiParam("供应商信息") @Validated @RequestBody Supplier supplier,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        supplierService.updateSupplier(supplier, operator != null ? operator : "system");
        return Result.success();
    }
}

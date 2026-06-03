package com.wms.controller;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Product;
import com.wms.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "商品管理接口")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品列表
     *
     * @param query       分页参数
     * @param productCode 商品编码
     * @param productName 商品名称
     * @param category    分类
     * @param brand       品牌
     * @param status      状态
     * @return 商品列表
     */
    @ApiOperation("商品列表")
    @GetMapping
    public Result<PageResult<Product>> getProductList(
            @ApiParam("分页参数") @Validated PageQuery query,
            @ApiParam("商品编码") @RequestParam(required = false) String productCode,
            @ApiParam("商品名称") @RequestParam(required = false) String productName,
            @ApiParam("分类") @RequestParam(required = false) String category,
            @ApiParam("品牌") @RequestParam(required = false) String brand,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        PageResult<Product> result = productService.queryProducts(
                query, productCode, productName, category, brand, status);
        return Result.success(result);
    }

    /**
     * 商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @ApiOperation("商品详情")
    @GetMapping("/{id}")
    public Result<Product> getProductById(@ApiParam("商品ID") @PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product);
    }

    /**
     * 新增商品
     *
     * @param product  商品信息
     * @param operator 操作人
     * @return 商品ID
     */
    @ApiOperation("新增商品")
    @PostMapping
    public Result<Long> createProduct(
            @ApiParam("商品信息") @Validated @RequestBody Product product,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        Long id = productService.createProduct(product, operator != null ? operator : "system");
        return Result.success(id);
    }

    /**
     * 更新商品
     *
     * @param product  商品信息
     * @param operator 操作人
     * @return 操作结果
     */
    @ApiOperation("更新商品")
    @PutMapping
    public Result<Void> updateProduct(
            @ApiParam("商品信息") @Validated @RequestBody Product product,
            @ApiParam("操作人") @RequestHeader(required = false) String operator) {
        productService.updateProduct(product, operator != null ? operator : "system");
        return Result.success();
    }
}

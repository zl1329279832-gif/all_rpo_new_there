package com.wms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.entity.Product;
import com.wms.exception.BusinessException;
import com.wms.mapper.ProductMapper;
import com.wms.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(Product product, String operator) {
        Product exist = productMapper.selectByCode(product.getProductCode());
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "商品编码已存在");
        }
        product.setCreateBy(operator);
        product.setCreateTime(new Date());
        product.setUpdateBy(operator);
        product.setUpdateTime(new Date());
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);
        log.info("创建商品成功: productCode={}, operator={}", product.getProductCode(), operator);
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Product product, String operator) {
        Product exist = getById(product.getId());
        if (!exist.getProductCode().equals(product.getProductCode())) {
            Product codeExist = productMapper.selectByCode(product.getProductCode());
            if (codeExist != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "商品编码已存在");
            }
        }
        product.setUpdateBy(operator);
        product.setUpdateTime(new Date());
        productMapper.updateById(product);
        log.info("更新商品成功: id={}, operator={}", product.getId(), operator);
    }

    @Override
    public PageResult<Product> queryProducts(PageQuery query, String productCode,
                                             String productName, String category,
                                             String brand, Integer status) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        java.util.List<Product> list = productMapper.selectList(query, productCode, productName, category, brand, status);
        PageInfo<Product> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(),
                pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "商品不存在");
        }
        return product;
    }

    @Override
    public Product getByCode(String productCode) {
        Product product = productMapper.selectByCode(productCode);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "商品不存在");
        }
        return product;
    }

    @Override
    public Product getByBarcode(String barcode) {
        Product product = productMapper.selectByBarcode(barcode);
        if (product == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "商品不存在");
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String operator) {
        getById(id);
        productMapper.updateStatus(id, status);
        log.info("更新商品状态成功: id={}, status={}, operator={}", id, status, operator);
    }
}

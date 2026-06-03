package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.entity.Product;

public interface ProductService {

    Long createProduct(Product product, String operator);

    void updateProduct(Product product, String operator);

    PageResult<Product> queryProducts(PageQuery query, String productCode,
                                      String productName, String category,
                                      String brand, Integer status);

    Product getById(Long id);

    Product getByCode(String productCode);

    Product getByBarcode(String barcode);

    void updateStatus(Long id, Integer status, String operator);
}

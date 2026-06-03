package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.entity.Supplier;

public interface SupplierService {

    Long createSupplier(Supplier supplier, String operator);

    void updateSupplier(Supplier supplier, String operator);

    PageResult<Supplier> querySuppliers(PageQuery query, String supplierCode,
                                        String supplierName, Integer creditLevel,
                                        Integer status);

    Supplier getById(Long id);

    Supplier getByCode(String supplierCode);

    void updateStatus(Long id, Integer status, String operator);
}

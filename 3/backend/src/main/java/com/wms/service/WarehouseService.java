package com.wms.service;

import com.wms.common.PageResult;
import com.wms.entity.Warehouse;

import java.util.List;

public interface WarehouseService {

    PageResult<Warehouse> queryWarehouses(Integer status);

    Warehouse getById(Long id);

    Warehouse getByCode(String warehouseCode);

    List<Warehouse> getAll();
}

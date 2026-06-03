package com.wms.service.impl;

import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.entity.Warehouse;
import com.wms.exception.BusinessException;
import com.wms.mapper.WarehouseMapper;
import com.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public PageResult<Warehouse> queryWarehouses(Integer status) {
        List<Warehouse> list = warehouseMapper.selectList(status);
        return PageResult.of(1, list.size(), (long) list.size(), list);
    }

    @Override
    public Warehouse getById(Long id) {
        Warehouse warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "仓库不存在");
        }
        return warehouse;
    }

    @Override
    public Warehouse getByCode(String warehouseCode) {
        Warehouse warehouse = warehouseMapper.selectByCode(warehouseCode);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "仓库不存在");
        }
        return warehouse;
    }

    @Override
    public List<Warehouse> getAll() {
        return warehouseMapper.selectList(1);
    }
}

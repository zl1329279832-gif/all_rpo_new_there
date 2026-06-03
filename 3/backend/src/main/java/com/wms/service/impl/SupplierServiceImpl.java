package com.wms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.entity.Supplier;
import com.wms.exception.BusinessException;
import com.wms.mapper.SupplierMapper;
import com.wms.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSupplier(Supplier supplier, String operator) {
        Supplier exist = supplierMapper.selectByCode(supplier.getSupplierCode());
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "供应商编码已存在");
        }
        supplier.setCreateBy(operator);
        supplier.setCreateTime(new Date());
        supplier.setUpdateBy(operator);
        supplier.setUpdateTime(new Date());
        if (supplier.getStatus() == null) {
            supplier.setStatus(1);
        }
        supplierMapper.insert(supplier);
        log.info("创建供应商成功: supplierCode={}, operator={}", supplier.getSupplierCode(), operator);
        return supplier.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(Supplier supplier, String operator) {
        Supplier exist = getById(supplier.getId());
        if (!exist.getSupplierCode().equals(supplier.getSupplierCode())) {
            Supplier codeExist = supplierMapper.selectByCode(supplier.getSupplierCode());
            if (codeExist != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "供应商编码已存在");
            }
        }
        supplier.setUpdateBy(operator);
        supplier.setUpdateTime(new Date());
        supplierMapper.updateById(supplier);
        log.info("更新供应商成功: id={}, operator={}", supplier.getId(), operator);
    }

    @Override
    public PageResult<Supplier> querySuppliers(PageQuery query, String supplierCode,
                                               String supplierName, Integer creditLevel,
                                               Integer status) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        java.util.List<Supplier> list = supplierMapper.selectList(query, supplierCode, supplierName, creditLevel, status);
        PageInfo<Supplier> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(),
                pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public Supplier getById(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "供应商不存在");
        }
        return supplier;
    }

    @Override
    public Supplier getByCode(String supplierCode) {
        Supplier supplier = supplierMapper.selectByCode(supplierCode);
        if (supplier == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "供应商不存在");
        }
        return supplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String operator) {
        getById(id);
        supplierMapper.updateStatus(id, status);
        log.info("更新供应商状态成功: id={}, status={}, operator={}", id, status, operator);
    }
}

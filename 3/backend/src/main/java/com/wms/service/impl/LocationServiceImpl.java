package com.wms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.common.ResultCode;
import com.wms.entity.Location;
import com.wms.exception.BusinessException;
import com.wms.mapper.LocationMapper;
import com.wms.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationMapper locationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLocation(Location location, String operator) {
        Location exist = locationMapper.selectByCode(location.getLocationCode());
        if (exist != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "库位编码已存在");
        }
        location.setCreateBy(operator);
        location.setCreateTime(new Date());
        location.setUpdateBy(operator);
        location.setUpdateTime(new Date());
        if (location.getStatus() == null) {
            location.setStatus(1);
        }
        if (location.getCurrentQuantity() == null) {
            location.setCurrentQuantity(BigDecimal.ZERO);
        }
        if (location.getAvailableCapacity() == null) {
            location.setAvailableCapacity(location.getMaxCapacity());
        }
        locationMapper.insert(location);
        log.info("创建库位成功: locationCode={}, operator={}", location.getLocationCode(), operator);
        return location.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLocation(Location location, String operator) {
        Location exist = getById(location.getId());
        if (!exist.getLocationCode().equals(location.getLocationCode())) {
            Location codeExist = locationMapper.selectByCode(location.getLocationCode());
            if (codeExist != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "库位编码已存在");
            }
        }
        location.setUpdateBy(operator);
        location.setUpdateTime(new Date());
        locationMapper.updateById(location);
        log.info("更新库位成功: id={}, operator={}", location.getId(), operator);
    }

    @Override
    public PageResult<Location> queryLocations(PageQuery query, Long warehouseId,
                                               Long areaId, Integer status, Integer locationType) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Location> list = locationMapper.selectList(warehouseId, areaId, status, locationType);
        PageInfo<Location> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(),
                pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public Location getById(Long id) {
        Location location = locationMapper.selectById(id);
        if (location == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "库位不存在");
        }
        return location;
    }

    @Override
    public Location getByCode(String locationCode) {
        Location location = locationMapper.selectByCode(locationCode);
        if (location == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "库位不存在");
        }
        return location;
    }

    @Override
    public List<Location> getAvailableLocations(Long warehouseId, Long areaId,
                                                BigDecimal requiredCapacity, Integer productType) {
        return locationMapper.selectAvailableLocation(warehouseId, areaId, requiredCapacity, productType);
    }

    @Override
    public List<Location> getLocationView(Long warehouseId, Long areaId) {
        return locationMapper.selectLocationView(warehouseId, areaId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String operator) {
        getById(id);
        locationMapper.updateStatus(id, status);
        log.info("更新库位状态成功: id={}, status={}, operator={}", id, status, operator);
    }
}

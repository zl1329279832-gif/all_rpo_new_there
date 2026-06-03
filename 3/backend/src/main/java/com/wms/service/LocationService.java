package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.entity.Location;

import java.math.BigDecimal;
import java.util.List;

public interface LocationService {

    Long createLocation(Location location, String operator);

    void updateLocation(Location location, String operator);

    PageResult<Location> queryLocations(PageQuery query, Long warehouseId,
                                        Long areaId, Integer status, Integer locationType);

    Location getById(Long id);

    Location getByCode(String locationCode);

    List<Location> getAvailableLocations(Long warehouseId, Long areaId,
                                         BigDecimal requiredCapacity, Integer productType);

    List<Location> getLocationView(Long warehouseId, Long areaId);

    void updateStatus(Long id, Integer status, String operator);
}

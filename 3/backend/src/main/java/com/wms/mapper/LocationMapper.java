package com.wms.mapper;

import com.wms.entity.Location;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface LocationMapper {

    int insert(Location record);

    int updateById(Location record);

    Location selectById(Long id);

    List<Location> selectList(@Param("warehouseId") Long warehouseId,
                               @Param("areaId") Long areaId,
                               @Param("status") Integer status,
                               @Param("locationType") Integer locationType);

    List<Location> selectAvailableLocation(@Param("warehouseId") Long warehouseId,
                                            @Param("areaId") Long areaId,
                                            @Param("requiredCapacity") BigDecimal requiredCapacity,
                                            @Param("productType") Integer productType);

    @Select("SELECT * FROM wms_location WHERE location_code = #{locationCode}")
    Location selectByCode(String locationCode);

    @Update("UPDATE wms_location SET current_quantity = current_quantity + #{quantity}, " +
            "available_capacity = available_capacity - #{quantity}, " +
            "status = CASE WHEN available_capacity - #{quantity} <= 0 THEN 2 ELSE status END, " +
            "update_time = NOW() WHERE id = #{id} AND available_capacity >= #{quantity}")
    int addQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_location SET current_quantity = current_quantity - #{quantity}, " +
            "available_capacity = available_capacity + #{quantity}, " +
            "status = CASE WHEN current_quantity - #{quantity} <= 0 THEN 1 ELSE status END, " +
            "update_time = NOW() WHERE id = #{id} AND current_quantity >= #{quantity}")
    int reduceQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_location SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    List<Location> selectLocationView(@Param("warehouseId") Long warehouseId, @Param("areaId") Long areaId);

    @Select("SELECT COUNT(*) FROM wms_location")
    int selectCount();
}

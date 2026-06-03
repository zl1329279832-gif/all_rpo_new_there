package com.wms.mapper;

import com.wms.entity.Warehouse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface WarehouseMapper {

    int insert(Warehouse record);

    int updateById(Warehouse record);

    Warehouse selectById(Long id);

    @Select("SELECT * FROM wms_warehouse WHERE warehouse_code = #{warehouseCode}")
    Warehouse selectByCode(String warehouseCode);

    List<Warehouse> selectList(@Param("status") Integer status);

    @Update("UPDATE wms_warehouse SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM wms_warehouse")
    int selectCount();
}

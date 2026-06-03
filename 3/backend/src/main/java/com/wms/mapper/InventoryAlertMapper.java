package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.InventoryAlert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InventoryAlertMapper {

    int insert(InventoryAlert record);

    int updateById(InventoryAlert record);

    InventoryAlert selectById(Long id);

    List<InventoryAlert> selectList(@Param("query") PageQuery query,
                                     @Param("alertType") Integer alertType,
                                     @Param("alertLevel") Integer alertLevel,
                                     @Param("status") Integer status,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("productId") Long productId);

    int countPending();

    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("handler") String handler, @Param("handleResult") String handleResult);

    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM wms_inventory_alert")
    int selectCount();
}

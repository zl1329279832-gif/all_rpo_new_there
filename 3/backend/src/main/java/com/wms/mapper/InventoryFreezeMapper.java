package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.InventoryFreeze;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

public interface InventoryFreezeMapper {

    int insert(InventoryFreeze record);

    int updateById(InventoryFreeze record);

    InventoryFreeze selectById(Long id);

    InventoryFreeze selectByNo(String freezeNo);

    List<InventoryFreeze> selectList(@Param("query") PageQuery query,
                                      @Param("freezeType") Integer freezeType,
                                      @Param("status") Integer status,
                                      @Param("warehouseId") Long warehouseId,
                                      @Param("productId") Long productId,
                                      @Param("batchNo") String batchNo);

    @Update("UPDATE wms_inventory_freeze SET status = 2, " +
            "unfreeze_time = #{unfreezeTime}, " +
            "unfreeze_operator = #{operator}, " +
            "unfreeze_reason = #{reason}, " +
            "update_time = NOW() WHERE id = #{id} AND status = 1")
    int unfreeze(@Param("id") Long id,
                 @Param("unfreezeTime") Date unfreezeTime,
                 @Param("operator") String operator,
                 @Param("reason") String reason);

    @Update("UPDATE wms_inventory_freeze SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

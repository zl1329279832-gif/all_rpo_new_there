package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.PickingTask;
import com.wms.entity.PickingTaskDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface PickingTaskMapper {

    int insert(PickingTask record);

    int updateById(PickingTask record);

    PickingTask selectById(Long id);

    PickingTask selectByNo(String taskNo);

    List<PickingTask> selectList(@Param("query") PageQuery query,
                                  @Param("warehouseId") Long warehouseId,
                                  @Param("shipmentOrderId") Long shipmentOrderId,
                                  @Param("picker") String picker,
                                  @Param("status") Integer status,
                                  @Param("priority") Integer priority);

    @Update("UPDATE wms_picking_task SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE wms_picking_task SET picked_items = picked_items + 1, " +
            "picked_quantity = picked_quantity + #{quantity}, " +
            "status = CASE WHEN picked_quantity + #{quantity} >= total_quantity THEN 3 ELSE 2 END, " +
            "finish_time = CASE WHEN picked_quantity + #{quantity} >= total_quantity THEN NOW() ELSE NULL END, " +
            "update_time = NOW() WHERE id = #{id}")
    int addPickedQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    int insertDetail(PickingTaskDetail detail);

    int updateDetail(PickingTaskDetail detail);

    List<PickingTaskDetail> selectDetailsByTaskId(Long taskId);

    PickingTaskDetail selectDetailById(Long id);

    int confirmPickingDetail(@Param("id") Long id,
                             @Param("pickedQuantity") BigDecimal pickedQuantity,
                             @Param("operator") String operator);
}

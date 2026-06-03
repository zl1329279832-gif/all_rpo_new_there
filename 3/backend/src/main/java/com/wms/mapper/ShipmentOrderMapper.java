package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.ShipmentAllocateDetail;
import com.wms.entity.ShipmentOrder;
import com.wms.entity.ShipmentOrderDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface ShipmentOrderMapper {

    int insert(ShipmentOrder record);

    int updateById(ShipmentOrder record);

    ShipmentOrder selectById(Long id);

    ShipmentOrder selectByNo(String shipmentNo);

    List<ShipmentOrder> selectList(@Param("query") PageQuery query,
                                    @Param("shipmentType") Integer shipmentType,
                                    @Param("orderStatus") Integer orderStatus,
                                    @Param("warehouseId") Long warehouseId,
                                    @Param("customerName") String customerName,
                                    @Param("startTime") java.util.Date startTime,
                                    @Param("endTime") java.util.Date endTime);

    @Update("UPDATE wms_shipment_order SET order_status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE wms_shipment_order SET picked_quantity = picked_quantity + #{quantity}, " +
            "update_time = NOW() WHERE id = #{id}")
    int addPickedQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    int insertDetail(ShipmentOrderDetail detail);

    int updateDetail(ShipmentOrderDetail detail);

    List<ShipmentOrderDetail> selectDetailsByOrderId(Long orderId);

    ShipmentOrderDetail selectDetailById(Long id);

    int insertAllocateDetail(ShipmentAllocateDetail detail);

    int updateAllocateDetail(ShipmentAllocateDetail detail);

    List<ShipmentAllocateDetail> selectAllocateDetailsByOrderId(Long orderId);

    List<ShipmentAllocateDetail> selectAllocateDetailsByDetailId(Long detailId);

    ShipmentAllocateDetail selectAllocateDetailById(Long id);

    @Update("UPDATE wms_shipment_allocate_detail SET picked_quantity = picked_quantity + #{quantity}, " +
            "is_picked = CASE WHEN picked_quantity + #{quantity} >= allocate_quantity THEN 1 ELSE 0 END, " +
            "update_time = NOW() WHERE id = #{id}")
    int addPickedQuantityToAllocate(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_shipment_allocate_detail SET reviewed_quantity = reviewed_quantity + #{quantity}, " +
            "is_reviewed = CASE WHEN reviewed_quantity + #{quantity} >= allocate_quantity THEN 1 ELSE 0 END, " +
            "update_time = NOW() WHERE id = #{id}")
    int addReviewedQuantityToAllocate(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    int deleteAllocateDetailsByOrderId(Long orderId);
}

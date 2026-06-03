package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.ReturnOrder;
import com.wms.entity.ReturnOrderDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface ReturnOrderMapper {

    int insert(ReturnOrder record);

    int updateById(ReturnOrder record);

    ReturnOrder selectById(Long id);

    ReturnOrder selectByNo(String returnNo);

    List<ReturnOrder> selectList(@Param("query") PageQuery query,
                                  @Param("returnType") Integer returnType,
                                  @Param("status") Integer status,
                                  @Param("warehouseId") Long warehouseId,
                                  @Param("originalShipmentNo") String originalShipmentNo);

    @Update("UPDATE wms_return_order SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE wms_return_order SET actual_quantity = actual_quantity + #{quantity}, " +
            "update_time = NOW() WHERE id = #{id}")
    int addActualQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    int insertDetail(ReturnOrderDetail detail);

    int updateDetail(ReturnOrderDetail detail);

    List<ReturnOrderDetail> selectDetailsByOrderId(Long orderId);

    ReturnOrderDetail selectDetailById(Long id);
}

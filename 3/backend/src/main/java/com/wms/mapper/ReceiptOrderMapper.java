package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.ReceiptOrder;
import com.wms.entity.ReceiptOrderDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface ReceiptOrderMapper {

    int insert(ReceiptOrder record);

    int updateById(ReceiptOrder record);

    ReceiptOrder selectById(Long id);

    ReceiptOrder selectByNo(String receiptNo);

    List<ReceiptOrder> selectList(@Param("query") PageQuery query,
                                   @Param("receiptType") Integer receiptType,
                                   @Param("orderStatus") Integer orderStatus,
                                   @Param("warehouseId") Long warehouseId,
                                   @Param("supplierId") Long supplierId,
                                   @Param("startTime") java.util.Date startTime,
                                   @Param("endTime") java.util.Date endTime);

    @Update("UPDATE wms_receipt_order SET order_status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE wms_receipt_order SET arrival_quantity = arrival_quantity + #{quantity}, " +
            "update_time = NOW() WHERE id = #{id}")
    int addArrivalQuantity(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    int insertDetail(ReceiptOrderDetail detail);

    int updateDetail(ReceiptOrderDetail detail);

    List<ReceiptOrderDetail> selectDetailsByOrderId(Long orderId);

    ReceiptOrderDetail selectDetailById(Long id);

    @Update("UPDATE wms_receipt_order_detail SET arrival_quantity = #{arrivalQuantity}, " +
            "qualified_quantity = #{qualifiedQuantity}, " +
            "unqualified_quantity = #{unqualifiedQuantity}, " +
            "actual_quantity = #{actualQuantity}, " +
            "inspection_result = #{inspectionResult}, " +
            "location_id = #{locationId}, " +
            "update_time = NOW() WHERE id = #{id}")
    int updateDetailForInspection(ReceiptOrderDetail detail);

    int deleteDetailById(Long id);
}

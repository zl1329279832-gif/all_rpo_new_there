package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.ShipmentOrderCreateDTO;
import com.wms.entity.ShipmentAllocateDetail;
import com.wms.entity.ShipmentOrder;
import com.wms.entity.ShipmentOrderDetail;

import java.util.Date;
import java.util.List;

public interface ShipmentOrderService {

    Long createShipmentOrder(ShipmentOrderCreateDTO dto, String operator);

    void allocateInventory(Long shipmentOrderId, String operator);

    void cancelShipment(Long shipmentOrderId, String cancelReason, String operator);

    void confirmShipmentComplete(Long shipmentOrderId, String operator);

    PageResult<ShipmentOrder> queryShipmentOrders(PageQuery query, Integer shipmentType,
                                                  Integer orderStatus, Long warehouseId,
                                                  String customerName, Date startTime, Date endTime);

    ShipmentOrder getById(Long id);

    ShipmentOrder getByNo(String shipmentNo);

    List<ShipmentOrderDetail> getDetailsByOrderId(Long orderId);

    List<ShipmentAllocateDetail> getAllocateDetailsByOrderId(Long orderId);
}

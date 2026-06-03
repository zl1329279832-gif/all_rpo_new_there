package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.entity.ReturnOrder;
import com.wms.entity.ReturnOrderDetail;

import java.math.BigDecimal;
import java.util.List;

public interface ReturnOrderService {

    Long createReturnOrder(ReturnOrder order, List<ReturnOrderDetail> details, String operator);

    void doReturnInspection(Long detailId, Integer inspectionResult,
                            BigDecimal actualQuantity, String operator);

    void returnToStock(Long detailId, Long locationId, String operator);

    void confirmReturnComplete(Long returnOrderId, String operator);

    PageResult<ReturnOrder> queryReturnOrders(PageQuery query, Integer returnType,
                                               Integer status, Long warehouseId,
                                               String originalShipmentNo);

    ReturnOrder getById(Long id);

    ReturnOrder getByNo(String returnNo);

    List<ReturnOrderDetail> getDetailsByOrderId(Long orderId);
}

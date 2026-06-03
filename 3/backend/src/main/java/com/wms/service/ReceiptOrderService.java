package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.ReceiptOrderCreateDTO;
import com.wms.entity.ReceiptOrder;
import com.wms.entity.ReceiptOrderDetail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface ReceiptOrderService {

    Long createReceiptOrder(ReceiptOrderCreateDTO dto, String operator);

    void confirmArrival(Long receiptOrderId, Date arrivalTime, String operator);

    void doInspection(Long detailId, BigDecimal arrivalQuantity, BigDecimal qualifiedQuantity,
                      BigDecimal unqualifiedQuantity, Integer inspectionResult,
                      String inspectionRemark, String operator);

    void assignLocation(Long detailId, Long locationId, String operator);

    void confirmReceiptComplete(Long receiptOrderId, String operator);

    PageResult<ReceiptOrder> queryReceiptOrders(PageQuery query, Integer receiptType,
                                                Integer orderStatus, Long warehouseId,
                                                Long supplierId, Date startTime, Date endTime);

    ReceiptOrder getById(Long id);

    ReceiptOrder getByNo(String receiptNo);

    List<ReceiptOrderDetail> getDetailsByOrderId(Long orderId);
}

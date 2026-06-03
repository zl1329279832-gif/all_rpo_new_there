package com.wms.service;

import com.wms.common.PageQuery;
import com.wms.common.PageResult;
import com.wms.dto.StocktakeResultDTO;
import com.wms.entity.StocktakeOrder;
import com.wms.entity.StocktakeOrderDetail;

import java.util.List;

public interface StocktakeService {

    Long createStocktakeOrder(StocktakeOrder order, String operator);

    void enterStocktakeResult(StocktakeResultDTO dto, String operator);

    void processDiff(Long detailId, Integer processStatus, String processResult, String operator);

    void confirmStocktakeComplete(Long stocktakeId, String operator);

    PageResult<StocktakeOrder> queryStocktakeOrders(PageQuery query, Integer stocktakeType,
                                                    Integer status, Long warehouseId,
                                                    Long areaId, String handler);

    StocktakeOrder getById(Long id);

    StocktakeOrder getByNo(String stocktakeNo);

    List<StocktakeOrderDetail> getDetailsByOrderId(Long orderId);
}

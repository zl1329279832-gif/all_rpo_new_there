package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.StocktakeOrder;
import com.wms.entity.StocktakeOrderDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface StocktakeOrderMapper {

    int insert(StocktakeOrder record);

    int updateById(StocktakeOrder record);

    StocktakeOrder selectById(Long id);

    StocktakeOrder selectByNo(String stocktakeNo);

    List<StocktakeOrder> selectList(@Param("query") PageQuery query,
                                     @Param("stocktakeType") Integer stocktakeType,
                                     @Param("status") Integer status,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("areaId") Long areaId,
                                     @Param("handler") String handler);

    @Update("UPDATE wms_stocktake_order SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE wms_stocktake_order SET count_quantity = count_quantity + #{countQuantity}, " +
            "profit_quantity = profit_quantity + #{profitQuantity}, " +
            "loss_quantity = loss_quantity + #{lossQuantity}, " +
            "update_time = NOW() WHERE id = #{id}")
    int addCountResult(@Param("id") Long id,
                       @Param("countQuantity") BigDecimal countQuantity,
                       @Param("profitQuantity") BigDecimal profitQuantity,
                       @Param("lossQuantity") BigDecimal lossQuantity);

    int insertDetail(StocktakeOrderDetail detail);

    int updateDetail(StocktakeOrderDetail detail);

    List<StocktakeOrderDetail> selectDetailsByOrderId(Long orderId);

    StocktakeOrderDetail selectDetailById(Long id);

    int updateDetailForCount(StocktakeOrderDetail detail);

    int updateDetailForProcess(@Param("id") Long id,
                                @Param("processStatus") Integer processStatus,
                                @Param("processResult") String processResult,
                                @Param("processor") String processor);

    int deleteDetailById(Long id);
}

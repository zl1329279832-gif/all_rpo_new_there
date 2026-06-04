package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bakery.entity.StockTransferDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockTransferDetailMapper extends BaseMapper<StockTransferDetail> {

    @Select("SELECT d.*, b.batch_no, r.product_name, b.expire_time, " +
            "(b.produce_qty - COALESCE((SELECT SUM(CASE WHEN biz_type='TRANSFER_OUT' THEN qty_change ELSE 0 END) FROM stock_log WHERE batch_id = d.batch_id), 0) - " +
            "COALESCE((SELECT SUM(CASE WHEN biz_type='DAMAGE' THEN qty_change ELSE 0 END) FROM stock_log WHERE batch_id = d.batch_id), 0) - " +
            "COALESCE((SELECT SUM(CASE WHEN biz_type='SALE' THEN qty_change ELSE 0 END) FROM stock_log WHERE batch_id = d.batch_id), 0)) as remain_qty " +
            "FROM stock_transfer_detail d " +
            "LEFT JOIN prod_batch b ON d.batch_id = b.id " +
            "LEFT JOIN base_recipe r ON d.recipe_id = r.id " +
            "WHERE d.transfer_id = #{transferId}")
    List<StockTransferDetail> selectByTransferId(@Param("transferId") Long transferId);
}

package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bakery.entity.StockTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockTransferMapper extends BaseMapper<StockTransfer> {

    @Select("<script>" +
            "SELECT t.*, o.store_name as from_store_name, i.store_name as to_store_name " +
            "FROM stock_transfer t " +
            "LEFT JOIN sys_store o ON t.out_store_id = o.id " +
            "LEFT JOIN sys_store i ON t.in_store_id = i.id " +
            "WHERE 1=1 " +
            "<if test='transferNo != null'>AND t.transfer_no LIKE CONCAT('%', #{transferNo}, '%')</if>" +
            "<if test='transferType != null'>AND t.transfer_type = #{transferType}</if>" +
            "<if test='status != null'>AND t.status = #{status}</if>" +
            "ORDER BY t.create_time DESC" +
            "</script>")
    IPage<StockTransfer> selectTransferPage(Page<StockTransfer> page,
                                             @Param("transferNo") String transferNo,
                                             @Param("transferType") Integer transferType,
                                             @Param("status") Integer status);
}

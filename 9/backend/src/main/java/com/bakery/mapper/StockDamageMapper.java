package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bakery.entity.StockDamage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockDamageMapper extends BaseMapper<StockDamage> {

    @Select("<script>" +
            "SELECT d.*, s.store_name " +
            "FROM stock_damage d " +
            "LEFT JOIN sys_store s ON d.store_id = s.id " +
            "WHERE 1=1 " +
            "<if test='damageType != null'>AND d.damage_type = #{damageType}</if>" +
            "<if test='status != null'>AND d.status = #{status}</if>" +
            "ORDER BY d.create_time DESC" +
            "</script>")
    IPage<StockDamage> selectDamagePage(Page<StockDamage> page,
                                        @Param("damageType") Integer damageType,
                                        @Param("status") Integer status);
}

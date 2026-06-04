package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bakery.entity.StoreDemand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StoreDemandMapper extends BaseMapper<StoreDemand> {

    @Select("<script>" +
            "SELECT d.*, s.store_name " +
            "FROM store_demand d " +
            "LEFT JOIN sys_store s ON d.store_id = s.id " +
            "WHERE 1=1 " +
            "<if test='status != null'>AND d.status = #{status}</if>" +
            "ORDER BY d.demand_date DESC, d.id DESC" +
            "</script>")
    IPage<StoreDemand> selectDemandPage(Page<StoreDemand> page, @Param("status") Integer status);
}

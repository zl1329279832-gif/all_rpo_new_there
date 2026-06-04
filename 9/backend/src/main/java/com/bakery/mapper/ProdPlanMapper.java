package com.bakery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bakery.entity.ProdPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface ProdPlanMapper extends BaseMapper<ProdPlan> {

    @Select("<script>" +
            "SELECT p.*, s.store_name FROM prod_plan p " +
            "LEFT JOIN sys_store s ON p.store_id = s.id " +
            "WHERE 1=1 " +
            "<if test='planDate != null'>AND p.plan_date = #{planDate}</if>" +
            "<if test='status != null'>AND p.status = #{status}</if>" +
            "ORDER BY p.plan_date DESC, p.id DESC" +
            "</script>")
    IPage<ProdPlan> selectPlanPage(Page<ProdPlan> page, @Param("planDate") LocalDate planDate, @Param("status") Integer status);
}

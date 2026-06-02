package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    @Select("SELECT ro.*, d.device_name, d.device_code FROM repair_order ro " +
            "LEFT JOIN device d ON ro.device_id = d.id " +
            "WHERE ro.id = #{id} AND ro.deleted = 0")
    RepairOrder selectWithDevice(@Param("id") Long id);

    @Select("SELECT status, COUNT(*) as count FROM repair_order WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT SUM(downtime) FROM repair_order WHERE status = 5 AND deleted = 0")
    Integer sumTotalDowntime();

    @Update("UPDATE repair_order SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT DATE_FORMAT(report_time, '%Y-%m') as month, COUNT(*) as count " +
            "FROM repair_order WHERE deleted = 0 AND report_time >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(report_time, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> countMonthlyTrend();
}

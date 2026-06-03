package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.InspectionTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface InspectionTaskMapper extends BaseMapper<InspectionTask> {

    @Select("SELECT it.*, d.device_name, d.device_code FROM inspection_task it " +
            "LEFT JOIN device d ON it.device_id = d.id " +
            "WHERE it.id = #{id} AND it.deleted = 0")
    InspectionTask selectWithDevice(@Param("id") Long id);

    @Select("SELECT * FROM inspection_task WHERE plan_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    List<InspectionTask> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT status, COUNT(*) as count FROM inspection_task WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT DATE_FORMAT(plan_date, '%Y-%m-%d') as date, COUNT(*) as count " +
            "FROM inspection_task " +
            "WHERE deleted = 0 AND plan_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(plan_date, '%Y-%m-%d') " +
            "ORDER BY date")
    List<Map<String, Object>> countLast7Days();

    @Select("SELECT it.*, d.device_name, d.device_code FROM inspection_task it " +
            "LEFT JOIN device d ON it.device_id = d.id " +
            "WHERE it.deleted = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR it.task_name LIKE CONCAT('%', #{keyword}, '%') OR it.task_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR it.status = #{status}) " +
            "AND (#{deviceId} IS NULL OR it.device_id = #{deviceId}) " +
            "AND (#{startDate} IS NULL OR it.plan_date >= #{startDate}) " +
            "AND (#{endDate} IS NULL OR it.plan_date <= #{endDate}) " +
            "ORDER BY it.id DESC")
    IPage<InspectionTask> selectPageWithDevice(IPage<InspectionTask> page,
                                               @Param("keyword") String keyword,
                                               @Param("status") Integer status,
                                               @Param("deviceId") Long deviceId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Select("SELECT it.*, d.device_name, d.device_code FROM inspection_task it " +
            "LEFT JOIN device d ON it.device_id = d.id " +
            "WHERE it.deleted = 0 " +
            "AND it.plan_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY it.plan_date")
    List<InspectionTask> selectByDateRangeWithDevice(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}

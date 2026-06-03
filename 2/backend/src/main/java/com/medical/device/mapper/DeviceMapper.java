package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    @Select("SELECT d.*, dept.dept_name FROM device d " +
            "LEFT JOIN department dept ON d.dept_id = dept.id " +
            "WHERE d.id = #{id} AND d.deleted = 0")
    Device selectDeviceWithDept(@Param("id") Long id);

    @Select("SELECT status, COUNT(*) as count FROM device WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT risk_level, COUNT(*) as count FROM device WHERE deleted = 0 GROUP BY risk_level")
    List<Map<String, Object>> countByRiskLevel();

    @Select("SELECT dept_id, COUNT(*) as count FROM device WHERE deleted = 0 GROUP BY dept_id")
    List<Map<String, Object>> countByDept();

    @Select("SELECT COUNT(*) FROM device WHERE risk_level = 1 AND deleted = 0")
    Long countHighRiskDevices();

    @Select("SELECT d.* FROM device d WHERE d.risk_level = 1 AND d.deleted = 0 ORDER BY d.id DESC")
    List<Device> selectHighRiskDevices();

    @Select("SELECT d.*, dept.dept_name FROM device d " +
            "LEFT JOIN department dept ON d.dept_id = dept.id " +
            "WHERE d.deleted = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR d.device_name LIKE CONCAT('%', #{keyword}, '%') OR d.device_code LIKE CONCAT('%', #{keyword}, '%') OR d.manufacturer LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR d.status = #{status}) " +
            "AND (#{riskLevel} IS NULL OR d.risk_level = #{riskLevel}) " +
            "AND (#{deptId} IS NULL OR d.dept_id = #{deptId}) " +
            "ORDER BY d.id DESC")
    IPage<Device> selectPageWithDept(IPage<Device> page,
                                     @Param("keyword") String keyword,
                                     @Param("status") Integer status,
                                     @Param("riskLevel") Integer riskLevel,
                                     @Param("deptId") Long deptId);
}

package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.QcRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface QcRecordMapper extends BaseMapper<QcRecord> {

    @Select("SELECT qc_result, COUNT(*) as count FROM qc_record WHERE deleted = 0 GROUP BY qc_result")
    List<Map<String, Object>> countByResult();

    @Select("SELECT qr.*, d.device_name, d.device_code FROM qc_record qr " +
            "LEFT JOIN device d ON qr.device_id = d.id " +
            "WHERE qr.deleted = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR qr.executor_name LIKE CONCAT('%', #{keyword}, '%') OR qr.qc_data LIKE CONCAT('%', #{keyword}, '%') OR qr.deviation_description LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{qcResult} IS NULL OR qr.qc_result = #{qcResult}) " +
            "AND (#{qcType} IS NULL OR qr.qc_type = #{qcType}) " +
            "AND (#{deviceId} IS NULL OR qr.device_id = #{deviceId}) " +
            "AND (#{startDate} IS NULL OR qr.qc_date >= #{startDate}) " +
            "AND (#{endDate} IS NULL OR qr.qc_date <= #{endDate}) " +
            "ORDER BY qr.qc_date DESC, qr.id DESC")
    IPage<QcRecord> selectPageWithDevice(IPage<QcRecord> page,
                                         @Param("keyword") String keyword,
                                         @Param("qcResult") Integer qcResult,
                                         @Param("qcType") String qcType,
                                         @Param("deviceId") Long deviceId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
}

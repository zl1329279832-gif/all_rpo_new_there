package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.QcRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QcRecordMapper extends BaseMapper<QcRecord> {

    @Select("SELECT qc_result, COUNT(*) as count FROM qc_record WHERE deleted = 0 GROUP BY qc_result")
    List<Map<String, Object>> countByResult();
}

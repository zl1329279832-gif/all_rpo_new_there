package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.SparePart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SparePartMapper extends BaseMapper<SparePart> {
}

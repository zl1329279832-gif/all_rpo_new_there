package com.medical.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.device.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}

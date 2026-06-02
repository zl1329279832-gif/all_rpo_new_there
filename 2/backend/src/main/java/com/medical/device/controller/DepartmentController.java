package com.medical.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.device.common.Result;
import com.medical.device.entity.Department;
import com.medical.device.mapper.DepartmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "科室管理", description = "科室信息管理")
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentMapper departmentMapper;

    @Operation(summary = "获取所有科室列表")
    @GetMapping
    public Result<List<Department>> listDepartments() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1).orderByAsc(Department::getSortOrder);
        List<Department> departments = departmentMapper.selectList(wrapper);
        return Result.success(departments);
    }

    @Operation(summary = "获取科室详情")
    @GetMapping("/{id}")
    public Result<Department> getDepartment(@PathVariable Long id) {
        Department department = departmentMapper.selectById(id);
        return Result.success(department);
    }

    @Operation(summary = "创建科室")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<String> createDepartment(@RequestBody Department department) {
        departmentMapper.insert(department);
        return Result.success("科室创建成功");
    }

    @Operation(summary = "更新科室")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<String> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        departmentMapper.updateById(department);
        return Result.success("科室更新成功");
    }

    @Operation(summary = "删除科室")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<String> deleteDepartment(@PathVariable Long id) {
        departmentMapper.deleteById(id);
        return Result.success("科室删除成功");
    }
}

package com.medical.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.common.Result;
import com.medical.device.entity.User;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.UserMapper;
import com.medical.device.service.AuthService;
import com.medical.device.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户信息管理接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = authService.getUserInfo(userId);
        return Result.success(user);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVICE_ADMIN')")
    public Result<PageResult<User>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getId);

        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(u -> u.setPassword(null));

        PageResult<User> result = PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "根据角色编码查询用户列表")
    @GetMapping("/listByRole")
    public Result<List<User>> listByRoleCode(@Parameter(description = "角色编码") @RequestParam String roleCode) {
        List<User> users = userMapper.selectByRoleCode(roleCode);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@RequestBody User user) {
        User exist = userMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setStatus(1);
        userMapper.insert(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> updateUser(@RequestBody User user) {
        User exist = userMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(exist.getPassword());
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("管理员账号不能删除");
        }
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

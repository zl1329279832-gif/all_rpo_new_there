package com.medical.device.service;

import com.medical.device.dto.LoginDTO;
import com.medical.device.entity.User;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.UserMapper;
import com.medical.device.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String roleCode = userMapper.selectUserRoleCode(user.getId());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCode);

        try {
            String tokenKey = "user:token:" + user.getId();
            redisTemplate.opsForValue().set(tokenKey, token, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis写入失败，token仅保存在JWT中: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", roleCode);

        return result;
    }

    public void logout(Long userId) {
        try {
            String tokenKey = "user:token:" + userId;
            redisTemplate.delete(tokenKey);
        } catch (Exception e) {
            log.warn("Redis删除失败: {}", e.getMessage());
        }
    }

    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }
}

package com.wms.service.impl;

import com.wms.common.ResultCode;
import com.wms.dto.LoginDTO;
import com.wms.entity.User;
import com.wms.exception.BusinessException;
import com.wms.lock.RedisLock;
import com.wms.mapper.UserMapper;
import com.wms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisLock redisLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User login(LoginDTO dto, String loginIp) {
        String lockKey = "user:login:" + dto.getUsername();
        return redisLock.executeWithLock(lockKey, () -> {
            User user = userMapper.selectByUsername(dto.getUsername());
            if (user == null) {
                log.warn("登录失败，用户不存在: username={}", dto.getUsername());
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            if (user.getStatus() == null || user.getStatus() == 0) {
                log.warn("登录失败，用户已禁用: username={}", dto.getUsername());
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            if (!user.getPassword().equals(dto.getPassword())) {
                log.warn("登录失败，密码错误: username={}", dto.getUsername());
                throw new BusinessException(ResultCode.PASSWORD_ERROR);
            }

            userMapper.updateLoginInfo(user.getId(), new Date(), loginIp);

            user.setPassword(null);

            log.info("用户登录成功: username={}, loginIp={}", dto.getUsername(), loginIp);
            return user;
        });
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User getByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }
}

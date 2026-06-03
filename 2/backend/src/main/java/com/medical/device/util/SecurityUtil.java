package com.medical.device.util;

import com.medical.device.exception.BusinessException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException("无法获取当前请求上下文");
        }
        Long userId = (Long) attrs.getRequest().getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("用户未登录或登录已过期");
        }
        return userId;
    }

    public static String getCurrentUsername() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException("无法获取当前请求上下文");
        }
        String username = (String) attrs.getRequest().getAttribute("username");
        if (username == null) {
            throw new BusinessException("用户未登录或登录已过期");
        }
        return username;
    }
}

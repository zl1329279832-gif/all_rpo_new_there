package com.wms.controller;

import com.wms.common.Result;
import com.wms.dto.LoginDTO;
import com.wms.entity.User;
import com.wms.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "认证接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param dto     登录信息
     * @param request HTTP请求
     * @return 登录用户信息
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<User> login(@ApiParam("登录信息") @Validated @RequestBody LoginDTO dto,
                              HttpServletRequest request) {
        String loginIp = request.getRemoteAddr();
        User user = userService.login(dto, loginIp);
        return Result.success(user);
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}

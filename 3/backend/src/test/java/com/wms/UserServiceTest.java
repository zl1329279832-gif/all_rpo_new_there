package com.wms;

import com.wms.dto.LoginDTO;
import com.wms.entity.User;
import com.wms.exception.BusinessException;
import com.wms.mapper.UserMapper;
import com.wms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@Rollback
@DisplayName("用户服务测试")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private String testUsername;
    private String testPassword;
    private String testLoginIp;

    @BeforeEach
    public void setUp() {
        testUsername = "admin";
        testPassword = "123456";
        testLoginIp = "127.0.0.1";
        log.info("初始化用户测试数据: username={}", testUsername);
    }

    @Test
    @DisplayName("测试用户登录 - 密码正确")
    public void testLoginSuccess() {
        log.info("开始测试用户登录 - 密码正确");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(testUsername);
        loginDTO.setPassword(testPassword);

        User user = userService.login(loginDTO, testLoginIp);

        assertNotNull(user, "登录成功应返回用户信息");
        assertEquals(testUsername, user.getUsername());
        assertNotNull(user.getRealName());
        log.info("用户登录成功: username={}, realName={}", user.getUsername(), user.getRealName());

        User updatedUser = userMapper.selectByUsername(testUsername);
        assertNotNull(updatedUser.getLastLoginTime(), "登录时间应更新");
        assertEquals(testLoginIp, updatedUser.getLastLoginIp(), "登录IP应更新");
        log.info("登录信息已更新: lastLoginTime={}, lastLoginIp={}",
                updatedUser.getLastLoginTime(), updatedUser.getLastLoginIp());

        assertNull(user.getPassword(), "返回的用户信息不应包含密码");
        log.info("用户登录(密码正确)测试完成");
    }

    @Test
    @DisplayName("测试用户登录 - 密码错误")
    public void testLoginWithWrongPassword() {
        log.info("开始测试用户登录 - 密码错误");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(testUsername);
        loginDTO.setPassword("wrongpassword");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO, testLoginIp);
        }, "密码错误应抛出异常");

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("密码") || exception.getMessage().contains("错误"),
                "异常信息应包含密码错误相关内容");
        log.info("密码错误登录测试完成，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试用户登录 - 用户不存在")
    public void testLoginWithNonExistentUser() {
        log.info("开始测试用户登录 - 用户不存在");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nonexistentuser");
        loginDTO.setPassword(testPassword);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO, testLoginIp);
        }, "用户不存在应抛出异常");

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("用户") || exception.getMessage().contains("不存在"),
                "异常信息应包含用户不存在相关内容");
        log.info("用户不存在登录测试完成，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试用户登录 - 用户已禁用")
    public void testLoginWithDisabledUser() {
        log.info("开始测试用户登录 - 用户已禁用");

        String disabledUsername = "disableduser";
        createTestUser(disabledUsername, testPassword, 0);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(disabledUsername);
        loginDTO.setPassword(testPassword);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO, testLoginIp);
        }, "禁用用户登录应抛出异常");

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("禁用") || exception.getMessage().contains("状态"),
                "异常信息应包含用户禁用相关内容");
        log.info("禁用用户登录测试完成，捕获异常: {}", exception.getMessage());
    }

    @Test
    @DisplayName("测试用户信息查询 - 根据ID查询")
    public void testGetById() {
        log.info("开始测试用户信息查询 - 根据ID查询");

        User existingUser = userMapper.selectByUsername(testUsername);
        assertNotNull(existingUser, "测试用户应存在");

        User user = userService.getById(existingUser.getId());
        assertNotNull(user);
        assertEquals(existingUser.getId(), user.getId());
        assertEquals(testUsername, user.getUsername());
        assertNotNull(user.getRealName());
        log.info("根据ID查询用户成功: id={}, username={}", user.getId(), user.getUsername());

        assertNull(user.getPassword(), "返回的用户信息不应包含密码");
        log.info("用户信息查询(根据ID)测试完成");
    }

    @Test
    @DisplayName("测试用户信息查询 - 根据用户名查询")
    public void testGetByUsername() {
        log.info("开始测试用户信息查询 - 根据用户名查询");

        User user = userService.getByUsername(testUsername);
        assertNotNull(user);
        assertEquals(testUsername, user.getUsername());
        assertNotNull(user.getRealName());
        assertNotNull(user.getPhone());
        log.info("根据用户名查询用户成功: username={}, realName={}", user.getUsername(), user.getRealName());

        assertNull(user.getPassword(), "返回的用户信息不应包含密码");
        log.info("用户信息查询(根据用户名)测试完成");
    }

    @Test
    @DisplayName("测试用户信息查询 - 不存在的用户ID返回null")
    public void testGetByIdNotFound() {
        log.info("开始测试用户信息查询 - 不存在的用户ID");

        User user = userService.getById(999999L);
        assertNull(user, "不存在的用户ID应返回null");
        log.info("不存在的用户ID查询测试完成");
    }

    @Test
    @DisplayName("测试用户信息查询 - 不存在的用户名返回null")
    public void testGetByUsernameNotFound() {
        log.info("开始测试用户信息查询 - 不存在的用户名");

        User user = userService.getByUsername("nonexistentuser");
        assertNull(user, "不存在的用户名应返回null");
        log.info("不存在的用户名查询测试完成");
    }

    @Test
    @DisplayName("测试用户登录 - 多次登录更新登录信息")
    public void testMultipleLoginUpdates() throws InterruptedException {
        log.info("开始测试用户登录 - 多次登录更新登录信息");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(testUsername);
        loginDTO.setPassword(testPassword);

        User firstLogin = userService.login(loginDTO, "192.168.1.1");
        assertNotNull(firstLogin);
        Date firstLoginTime = userMapper.selectByUsername(testUsername).getLastLoginTime();
        log.info("第一次登录: ip=192.168.1.1, time={}", firstLoginTime);

        Thread.sleep(1000);

        User secondLogin = userService.login(loginDTO, "192.168.1.2");
        assertNotNull(secondLogin);
        User updatedUser = userMapper.selectByUsername(testUsername);
        Date secondLoginTime = updatedUser.getLastLoginTime();
        String secondLoginIp = updatedUser.getLastLoginIp();
        log.info("第二次登录: ip={}, time={}", secondLoginIp, secondLoginTime);

        assertTrue(secondLoginTime.after(firstLoginTime) || secondLoginTime.equals(firstLoginTime),
                "第二次登录时间应晚于或等于第一次登录时间");
        assertEquals("192.168.1.2", secondLoginIp, "登录IP应更新为第二次的IP");
        log.info("多次登录更新信息测试完成");
    }

    @Test
    @DisplayName("测试用户登录 - 密码正确验证用户字段完整性")
    public void testLoginUserFields() {
        log.info("开始测试用户登录 - 验证返回用户字段完整性");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(testUsername);
        loginDTO.setPassword(testPassword);

        User user = userService.login(loginDTO, testLoginIp);

        assertNotNull(user);
        assertNotNull(user.getId(), "用户ID不应为null");
        assertNotNull(user.getUsername(), "用户名不应为null");
        assertNotNull(user.getRealName(), "真实姓名不应为null");
        assertNotNull(user.getPhone(), "手机号不应为null");
        assertNotNull(user.getStatus(), "状态不应为null");
        assertEquals(1, user.getStatus(), "状态应为启用");

        log.info("用户字段验证完成: id={}, username={}, realName={}, phone={}, status={}",
                user.getId(), user.getUsername(), user.getRealName(), user.getPhone(), user.getStatus());
    }

    @Test
    @DisplayName("测试创建测试用户并登录")
    public void testCreateAndLoginNewUser() {
        log.info("开始测试创建测试用户并登录");

        String newUsername = "testuser_" + System.currentTimeMillis();
        String newPassword = "test123456";
        createTestUser(newUsername, newPassword, 1);

        User createdUser = userMapper.selectByUsername(newUsername);
        assertNotNull(createdUser);
        assertEquals(newUsername, createdUser.getUsername());
        log.info("创建测试用户成功: username={}", newUsername);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(newUsername);
        loginDTO.setPassword(newPassword);

        User loggedInUser = userService.login(loginDTO, "10.0.0.1");
        assertNotNull(loggedInUser);
        assertEquals(newUsername, loggedInUser.getUsername());
        log.info("新用户登录成功: username={}", loggedInUser.getUsername());

        log.info("创建并登录新用户测试完成");
    }

    @Test
    @DisplayName("测试用户登录 - 密码大小写敏感")
    public void testLoginCaseSensitivePassword() {
        log.info("开始测试用户登录 - 密码大小写敏感");

        String newUsername = "caseuser_" + System.currentTimeMillis();
        String password = "TestPass123";
        createTestUser(newUsername, password, 1);

        LoginDTO correctLogin = new LoginDTO();
        correctLogin.setUsername(newUsername);
        correctLogin.setPassword(password);
        User user = userService.login(correctLogin, testLoginIp);
        assertNotNull(user, "正确密码应登录成功");
        log.info("正确密码登录成功");

        LoginDTO wrongCaseLogin = new LoginDTO();
        wrongCaseLogin.setUsername(newUsername);
        wrongCaseLogin.setPassword("testpass123");
        assertThrows(BusinessException.class, () -> {
            userService.login(wrongCaseLogin, testLoginIp);
        }, "错误大小写的密码应登录失败");
        log.info("错误大小写密码登录失败，验证了大小写敏感性");

        log.info("密码大小写敏感测试完成");
    }

    @Test
    @DisplayName("测试用户信息查询 - 验证不返回密码字段")
    public void testUserInfoNoPassword() {
        log.info("开始测试用户信息查询 - 验证不返回密码字段");

        User user1 = userService.getByUsername(testUsername);
        assertNull(user1.getPassword(), "getByUsername返回的用户不应包含密码");
        log.info("getByUsername不返回密码字段 - 验证通过");

        User existingUser = userMapper.selectByUsername(testUsername);
        User user2 = userService.getById(existingUser.getId());
        assertNull(user2.getPassword(), "getById返回的用户不应包含密码");
        log.info("getById不返回密码字段 - 验证通过");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(testUsername);
        loginDTO.setPassword(testPassword);
        User user3 = userService.login(loginDTO, testLoginIp);
        assertNull(user3.getPassword(), "login返回的用户不应包含密码");
        log.info("login不返回密码字段 - 验证通过");

        log.info("用户信息不返回密码字段测试完成");
    }

    private void createTestUser(String username, String password, Integer status) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName("测试用户");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setDepartment("测试部");
        user.setPosition("测试工程师");
        user.setWarehouseId(1L);
        user.setStatus(status);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        log.info("创建测试用户: username={}, status={}", username, status);
    }
}

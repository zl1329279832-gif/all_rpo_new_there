package com.medical.device.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "medical-device-management-system-secret-key-2024-test");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        String username = jwtUtil.getUsername(token);
        assertEquals("admin", username);
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken(100L, "admin", "ADMIN");
        Long userId = jwtUtil.getUserId(token);
        assertEquals(100L, userId);
    }

    @Test
    void testGetRoleFromToken() {
        String token = jwtUtil.generateToken(1L, "admin", "DEVICE_ADMIN");
        String role = jwtUtil.getRole(token);
        assertEquals("DEVICE_ADMIN", role);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        assertTrue(jwtUtil.validateToken(token, "admin"));
        assertFalse(jwtUtil.validateToken(token, "other"));
    }

    @Test
    void testParseToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ADMIN");
        Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
    }
}

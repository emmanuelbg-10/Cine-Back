package com.eviden.cine.security;

import com.eviden.cine.model.Role;
import com.eviden.cine.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String secret = "ZmFrZXNlY3JldGZvcmp3dHRlc3R1c2VzaWduYXR1cmVzaWduZWQ="; // base64-encoded 256-bit key
    private final long expiration = 3600000; // 1 hora

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Inyectar valores privados simulados
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", expiration);

        // Usuario de prueba
        Role role = new Role();
        role.setName("USER");

        testUser = new User();
        testUser.setUserId(123L);
        testUser.setEmail("test@example.com");
        testUser.setRole(role);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken(testUser);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractEmailAndUserIdFromToken() {
        String token = jwtUtil.generateToken(testUser);
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
        assertEquals(123L, jwtUtil.extractUserId(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.value"));
    }
}

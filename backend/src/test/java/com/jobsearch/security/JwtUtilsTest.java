package com.jobsearch.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "mySecretKeyForJWTTokenGenerationMustBe256BitsLongAtLeast1234567890AbCdEf");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", 86400000L);
    }

    @Test
    void generateToken_ReturnsNonNullToken() {
        String token = jwtUtils.generateToken("test@gmail.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getEmailFromToken_ReturnsCorrectEmail() {
        String token = jwtUtils.generateToken("test@gmail.com");
        String email = jwtUtils.getEmailFromToken(token);
        assertEquals("test@gmail.com", email);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtils.generateToken("test@gmail.com");
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        assertFalse(jwtUtils.validateToken("not.a.valid.jwt.token"));
    }

    @Test
    void generateToken_DifferentEmails_ReturnsDifferentTokens() {
        String token1 = jwtUtils.generateToken("user1@gmail.com");
        String token2 = jwtUtils.generateToken("user2@gmail.com");
        assertNotEquals(token1, token2);
    }
}

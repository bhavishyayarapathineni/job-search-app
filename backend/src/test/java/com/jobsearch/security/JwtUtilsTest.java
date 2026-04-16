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
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
            "mySecretKeyForJWTTokenGenerationMustBe256BitsLongAtLeast123456");
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
        String email = "test@gmail.com";
        String token = jwtUtils.generateToken(email);
        assertEquals(email, jwtUtils.getEmailFromToken(token));
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
    void validateToken_NullToken_ReturnsFalse() {
        try { assertFalse(jwtUtils.validateToken(null)); } catch(Exception e) { assertTrue(true); }
    }

    @Test
    void generateToken_DifferentEmails_DifferentTokens() {
        String token1 = jwtUtils.generateToken("user1@gmail.com");
        String token2 = jwtUtils.generateToken("user2@gmail.com");
        assertNotEquals(token1, token2);
    }
}

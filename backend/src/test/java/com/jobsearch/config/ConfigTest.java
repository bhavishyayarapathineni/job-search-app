package com.jobsearch.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConfigTest {

    @Test
    void securityConfig_PasswordEncoder_NotNull() {
        SecurityConfig config = new SecurityConfig(null);
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
    }

    @Test
    void securityConfig_PasswordEncoder_EncodesPassword() {
        SecurityConfig config = new SecurityConfig(null);
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("password123");
        assertNotNull(encoded);
        assertNotEquals("password123", encoded);
    }

    @Test
    void securityConfig_PasswordEncoder_MatchesCorrectPassword() {
        SecurityConfig config = new SecurityConfig(null);
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("password123");
        assertTrue(encoder.matches("password123", encoded));
    }

    @Test
    void securityConfig_PasswordEncoder_RejectsWrongPassword() {
        SecurityConfig config = new SecurityConfig(null);
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("password123");
        assertFalse(encoder.matches("wrongpassword", encoded));
    }

    @Test
    void securityConfig_PasswordEncoder_DifferentHashEachTime() {
        SecurityConfig config = new SecurityConfig(null);
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded1 = encoder.encode("password123");
        String encoded2 = encoder.encode("password123");
        assertNotEquals(encoded1, encoded2);
    }

    @Test
    void swaggerConfig_NotNull() {
        SwaggerConfig config = new SwaggerConfig();
        assertNotNull(config);
    }

    @Test
    void securityConfig_CorsConfig_NotNull() {
        SecurityConfig config = new SecurityConfig(null);
        assertNotNull(config.corsConfigurationSource());
    }

}
package com.jobsearch.config;

import com.jobsearch.security.JwtAuthFilter;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConfigTest {

    @Test
    void kafkaConfig_CreatesExpectedTopic() {
        KafkaConfig config = new KafkaConfig();
        NewTopic topic = config.newJobsTopic();

        assertEquals("new-jobs", topic.name());
        assertEquals(1, topic.numPartitions());
        assertEquals(1, topic.replicationFactor());
    }

    @Test
    void securityConfig_CorsAndPasswordEncoderBeansWork() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class));

        CorsConfigurationSource source = config.corsConfigurationSource();
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);

        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));
        assertNotNull(cors);
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertEquals(true, cors.getAllowCredentials());

        PasswordEncoder encoder = config.passwordEncoder();
        assertTrue(encoder.matches("password123", encoder.encode("password123")));
    }

    @Test
    void swaggerConfig_CanBeInstantiated() {
        SwaggerConfig config = new SwaggerConfig();
        assertNotNull(config);
    }
}

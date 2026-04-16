package com.jobsearch.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void authResponse_GettersSetters() {
        AuthResponse response = new AuthResponse(
            "token123", "test@gmail.com", "Test User", "Login successful!"
        );
        assertEquals("token123", response.getToken());
        assertEquals("test@gmail.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("Login successful!", response.getMessage());
    }

    @Test
    void loginRequest_GettersSetters() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        assertEquals("test@gmail.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void registerRequest_GettersSetters() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setPhone("1234567890");
        assertEquals("test@gmail.com", request.getEmail());
        assertEquals("Test User", request.getFullName());
        assertEquals("1234567890", request.getPhone());
    }
}

package com.jobsearch.controller;

import com.jobsearch.dto.AuthResponse;
import com.jobsearch.dto.LoginRequest;
import com.jobsearch.dto.RegisterRequest;
import com.jobsearch.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController controller;

    @Test
    void register_ReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        AuthResponse response = new AuthResponse("token", "email@test.com", "Test User", "ok");
        when(authService.register(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = controller.register(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void login_ReturnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        AuthResponse response = new AuthResponse("token", "email@test.com", "Test User", "ok");
        when(authService.login(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = controller.login(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
    }

    @Test
    void health_ReturnsExpectedMessage() {
        ResponseEntity<String> result = controller.health();

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Auth service is running!", result.getBody());
    }
}

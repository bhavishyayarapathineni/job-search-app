package com.jobsearch.service;

import com.jobsearch.dto.AuthResponse;
import com.jobsearch.dto.LoginRequest;
import com.jobsearch.dto.RegisterRequest;
import com.jobsearch.model.User;
import com.jobsearch.repository.UserRepository;
import com.jobsearch.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        // Check if email already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user — never store plain password!
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());

        userRepository.save(user);

        // Generate JWT token for immediate login after register
        String token = jwtUtils.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail(),
                user.getFullName(), "Registration successful!");
    }

    public AuthResponse login(LoginRequest request) {

        // Spring Security checks email + password for us
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        // If we get here, credentials are correct
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtUtils.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail(),
                user.getFullName(), "Login successful!");
    }
}

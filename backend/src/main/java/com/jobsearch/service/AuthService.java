package com.jobsearch.service;

import com.jobsearch.dto.AuthResponse;
import com.jobsearch.dto.LoginRequest;
import com.jobsearch.dto.RegisterRequest;
import com.jobsearch.model.User;
import com.jobsearch.repository.UserRepository;
import com.jobsearch.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        userRepository.save(user);
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(),
                user.getFullName(), "Registration successful!");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(),
                user.getFullName(), "Login successful!");
    }
}

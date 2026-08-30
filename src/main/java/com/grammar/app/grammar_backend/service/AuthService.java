package com.grammar.app.grammar_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grammar.app.grammar_backend.dto.AuthResponse;
import com.grammar.app.grammar_backend.dto.LoginRequest;
import com.grammar.app.grammar_backend.dto.RegisterRequest;
import com.grammar.app.grammar_backend.dto.UserDto;
import com.grammar.app.grammar_backend.entity.User;
import com.grammar.app.grammar_backend.repository.UserRepository;
import com.grammar.app.grammar_backend.util.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username is already registered");
        }

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken,
                new UserDto(user.getId(), user.getEmail(), user.getUsername()));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken,
                new UserDto(user.getId(), user.getEmail(), user.getUsername()));
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        return new AuthResponse(newAccessToken, refreshToken,
                new UserDto(user.getId(), user.getEmail(), user.getUsername()));
    }

}

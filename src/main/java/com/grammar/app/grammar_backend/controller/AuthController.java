package com.grammar.app.grammar_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grammar.app.grammar_backend.dto.AuthResponse;
import com.grammar.app.grammar_backend.dto.LoginRequest;
import com.grammar.app.grammar_backend.dto.RegisterRequest;
import com.grammar.app.grammar_backend.dto.UserDto;
import com.grammar.app.grammar_backend.entity.User;
import com.grammar.app.grammar_backend.repository.UserRepository;
import com.grammar.app.grammar_backend.security.CustomUserDetails;
import com.grammar.app.grammar_backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:3000}")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new account and receive authentication tokens")
    @Parameter(name = "email", description = "User's email address", required = true)
    @Parameter(name = "username", description = "User's username", required = true)
    @Parameter(name = "password", description = "User's password", required = true)
    public ResponseEntity<AuthResponse> register(String email, String username, String password) {
        RegisterRequest request = new RegisterRequest(email, username, password);
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Parameter(name = "email", description = "User's email address", required = true)
    @Parameter(name = "password", description = "User's password", required = true)
    @Operation(summary = "Login a user", description = "Authenticate a user and receive authentication tokens")
    public ResponseEntity<AuthResponse> login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me")
    @Operation(summary = "Get current user info", description = "Retrieve information about the currently authenticated user")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserDto> getCurrentUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return ResponseEntity.ok(new UserDto(user.getId(), user.getEmail(), user.getUsername()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh authentication tokens", description = "Generate new authentication tokens using a valid refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.startsWith("Bearer ")
                ? refreshToken.substring(7)
                : refreshToken;

        AuthResponse response = authService.refreshAccessToken(token);
        return ResponseEntity.ok(response);
    }

}

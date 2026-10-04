package com.grammar.app.grammar_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.grammar.app.grammar_backend.dto.AuthResponse;
import com.grammar.app.grammar_backend.dto.LoginRequest;
import com.grammar.app.grammar_backend.dto.RegisterRequest;
import com.grammar.app.grammar_backend.dto.UserDto;
import com.grammar.app.grammar_backend.entity.User;
import com.grammar.app.grammar_backend.repository.UserRepository;
import com.grammar.app.grammar_backend.util.JwtUtil;
import java.util.Optional;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final Long USER_ID = 42L;
  private static final String EMAIL = "learner@example.com";
  private static final String USERNAME = "learner";
  private static final String PASSWORD = "password123";
  private static final String PASSWORD_HASH = "encoded-password";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String REFRESH_TOKEN = "refresh-token";

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private AuthService authService;

  private User user;

  @BeforeEach
    void setUp() {
        user = User.builder()
        .id(USER_ID)
        .email(EMAIL)
        .username(USERNAME)
        .passwordHash(PASSWORD_HASH)
        .build();
    }

  @Test
  void testRegisterWhenEmailIsAlreadyRegistered() {
    Mockito.when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> authService.register(new RegisterRequest(EMAIL, USERNAME, PASSWORD))
    );

    assertEquals("Email is already registered", exception.getMessage());
    Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
  }

  @Test
  void testRegisterWhenUsernameIsAlreadyRegistered() {
    Mockito.when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
    Mockito.when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> authService.register(new RegisterRequest(EMAIL, USERNAME, PASSWORD))
    );

    assertEquals("Username is already registered", exception.getMessage());
    Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
  }

  @Test
  void testRegister() {
    Mockito.when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
    Mockito.when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
    Mockito.when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
    Mockito.when(userRepository.save(any(User.class))).thenAnswer(
      invocation -> {
        User user = invocation.getArgument(0);
        user.setId(USER_ID);
        return user;
      }
    );
    Mockito.when(jwtUtil.generateAccessToken(USER_ID, EMAIL)).thenReturn(
      ACCESS_TOKEN
    );
    Mockito.when(jwtUtil.generateRefreshToken(USER_ID)).thenReturn(
      REFRESH_TOKEN
    );

    AuthResponse response = authService.register(
      new RegisterRequest(EMAIL, USERNAME, PASSWORD)
    );

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    assertEquals(PASSWORD_HASH, userCaptor.getValue().getPasswordHash());
    assertEquals(ACCESS_TOKEN, response.accessToken());
    assertEquals(REFRESH_TOKEN, response.refreshToken());
    assertEquals(new UserDto(USER_ID, EMAIL, USERNAME), response.user());
    verify(passwordEncoder).encode(PASSWORD);
  }

  @Test
  void testLogin() {
    Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(
      Optional.of(user)
    );
    Mockito.when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(
      true
    );
    Mockito.when(jwtUtil.generateAccessToken(USER_ID, EMAIL)).thenReturn(
      ACCESS_TOKEN
    );
    Mockito.when(jwtUtil.generateRefreshToken(USER_ID)).thenReturn(
      REFRESH_TOKEN
    );

    AuthResponse response = authService.login(
      new LoginRequest(EMAIL, PASSWORD)
    );

    assertEquals(ACCESS_TOKEN, response.accessToken());
    assertEquals(REFRESH_TOKEN, response.refreshToken());
    assertEquals(new UserDto(USER_ID, EMAIL, USERNAME), response.user());
  }

  @Test
  void testLoginEmailDoesNotExist() {
    Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(
      Optional.empty()
    );

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> authService.login(new LoginRequest(EMAIL, PASSWORD))
    );

    assertEquals("Invalid email or password", exception.getMessage());
  }

  @Test
  void testLoginPasswordDoesNotMatch() {
    Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(
      Optional.of(user)
    );
    Mockito.when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(
      false
    );

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> authService.login(new LoginRequest(EMAIL, PASSWORD))
    );

    assertEquals("Invalid email or password", exception.getMessage());
  }

  @Test
  void testRefreshAccessToken() {
    Mockito.when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
    Mockito.when(jwtUtil.extractUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
    Mockito.when(userRepository.findById(USER_ID)).thenReturn(
      Optional.of(user)
    );
    Mockito.when(jwtUtil.generateAccessToken(USER_ID, EMAIL)).thenReturn(
      ACCESS_TOKEN
    );

    AuthResponse response = authService.refreshAccessToken(REFRESH_TOKEN);

    assertEquals(ACCESS_TOKEN, response.accessToken());
    assertEquals(REFRESH_TOKEN, response.refreshToken());
    assertEquals(new UserDto(USER_ID, EMAIL, USERNAME), response.user());
  }

  @Test
  void testRefreshAccessTokenTokenIsExpired() {
    Mockito.when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(true);

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> authService.refreshAccessToken(REFRESH_TOKEN)
    );

    assertEquals("Invalid or expired refresh token", exception.getMessage());
    Mockito.verify(jwtUtil, Mockito.never()).extractUserId(REFRESH_TOKEN);
    Mockito.verify(userRepository, Mockito.never()).findById(USER_ID);
  }

  @Test
  void testRefreshAccessTokenUserDoesNotExist() {
    Mockito.when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
    Mockito.when(jwtUtil.extractUserId(REFRESH_TOKEN)).thenReturn(USER_ID);
    Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> authService.refreshAccessToken(REFRESH_TOKEN)
    );

    assertEquals("User not found", exception.getMessage());
  }
}

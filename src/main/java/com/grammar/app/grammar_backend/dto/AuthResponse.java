package com.grammar.app.grammar_backend.dto;

public record AuthResponse(
        String accessToken, String refreshToken, UserDto user) {

}

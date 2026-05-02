package com.example.backend.dto.auth;

public record RegisterResponse(
        Long userId,
        String userName
) {
}

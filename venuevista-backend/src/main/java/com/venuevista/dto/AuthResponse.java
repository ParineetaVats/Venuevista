package com.venuevista.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        UserResponse user
) {
    public AuthResponse(String token, Instant expiresAt, UserResponse user) {
        this(token, "Bearer", expiresAt, user);
    }
}

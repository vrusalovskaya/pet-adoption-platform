package com.wise.petadoption.security.api;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}

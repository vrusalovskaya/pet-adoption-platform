package com.wise.petadoption.security.auth.controller;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}

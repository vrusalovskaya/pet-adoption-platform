package com.wise.petadoption.security.auth;

public record AuthenticationResult(
        String accessToken,
        String refreshToken
) {
}

package com.wise.petadoption.security.domain;

public record AuthenticationResult(
        String accessToken,
        String refreshToken
) {
}

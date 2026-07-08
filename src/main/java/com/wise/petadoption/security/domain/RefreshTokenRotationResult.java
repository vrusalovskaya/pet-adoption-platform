package com.wise.petadoption.security.domain;

public record RefreshTokenRotationResult(
        String rawToken,
        Long userId
) {
}

package com.wise.petadoption.security.domain;

import java.time.Instant;

public record RefreshToken(
        Long id,
        Instant expiresAt,
        Long userId
) {
}

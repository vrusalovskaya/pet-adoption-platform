package com.wise.petadoption.security.auth.refresh;

import java.time.Instant;

public record RefreshToken(
        Long id,
        Instant expiresAt,
        Long userId
) {
}

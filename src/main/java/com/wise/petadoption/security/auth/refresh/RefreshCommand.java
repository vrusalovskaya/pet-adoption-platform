package com.wise.petadoption.security.auth.refresh;

public record RefreshCommand(
        String refreshToken
) {
}

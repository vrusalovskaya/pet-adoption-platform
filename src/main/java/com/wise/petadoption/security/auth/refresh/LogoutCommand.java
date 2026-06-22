package com.wise.petadoption.security.auth.refresh;

public record LogoutCommand(
        String refreshToken
) {
}

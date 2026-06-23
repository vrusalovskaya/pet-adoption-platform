package com.wise.petadoption.security.domain;

public record LogoutCommand(
        String refreshToken
) {
}

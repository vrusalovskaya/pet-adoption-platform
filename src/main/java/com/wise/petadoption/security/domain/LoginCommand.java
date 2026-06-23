package com.wise.petadoption.security.domain;

public record LoginCommand(
        String email,
        String password
) {
}

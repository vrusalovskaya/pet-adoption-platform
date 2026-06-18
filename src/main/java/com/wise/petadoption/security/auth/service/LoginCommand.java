package com.wise.petadoption.security.auth.service;

public record LoginCommand(
        String email,
        String password
) {
}

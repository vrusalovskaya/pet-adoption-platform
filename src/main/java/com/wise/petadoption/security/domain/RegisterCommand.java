package com.wise.petadoption.security.domain;

public record RegisterCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone
) {
}

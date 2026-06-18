package com.wise.petadoption.security.auth.service;

public record RegisterCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phone
) {
}

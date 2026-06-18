package com.wise.petadoption.user;

public record CreateUserCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        String phone,
        Role role
) {
}

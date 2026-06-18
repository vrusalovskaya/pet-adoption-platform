package com.wise.petadoption.user;

public record ModifyUserCommand(
        Long id,
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        String phone,
        Role role
) {
}

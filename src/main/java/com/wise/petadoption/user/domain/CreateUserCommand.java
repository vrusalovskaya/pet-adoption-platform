package com.wise.petadoption.user.domain;

import com.wise.petadoption.user.common.Role;

public record CreateUserCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        String phone,
        Role role
) {
}

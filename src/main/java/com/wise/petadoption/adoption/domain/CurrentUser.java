package com.wise.petadoption.adoption.domain;

import com.wise.petadoption.user.common.Role;

public record CurrentUser(
        Long id,
        Role role
) {
    public boolean isAdmin() {
        return role == Role.ROLE_ADMIN;
    }
}

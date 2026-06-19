package com.wise.petadoption.user.domain;

import com.wise.petadoption.user.common.Role;

import java.time.LocalDateTime;

public record User(
        Long id,
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String phone,
        Role role,
        LocalDateTime createdAt
) {
}

package com.wise.petadoption.user.domain;

public record UpdateProfileCommand(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone
) {
}

package com.wise.petadoption.user.domain;

public record UpdateProfileCommand(
        String email,
        String firstName,
        String lastName,
        String phone
) {
}

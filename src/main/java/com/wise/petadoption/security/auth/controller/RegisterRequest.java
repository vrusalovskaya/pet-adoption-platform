package com.wise.petadoption.security.auth.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email
        @NotBlank(message = "Email id is mandatory")
        String email,

        @Size(min = 8, max = 100)
        @NotBlank(message = "Password id is mandatory")
        String password,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        String phone
) {
}

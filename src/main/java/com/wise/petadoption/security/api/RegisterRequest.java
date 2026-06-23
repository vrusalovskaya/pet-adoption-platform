package com.wise.petadoption.security.api;

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

        @NotBlank(message = "Name is mandatory")
        @Size(max = 50, message = "Name length should not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Surname is mandatory")
        @Size(max = 50, message = "Surname length should not exceed 50 characters")
        String lastName,

        @Size(max = 20, message = "Phone length should not exceed 50 characters")
        String phone
) {
}

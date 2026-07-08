package com.wise.petadoption.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Email
        @NotBlank(message = "Email id is mandatory")
        String email,

        @NotNull(message = "Name is mandatory")
        @Size(max = 50, message = "Name length should not exceed 50 characters")
        String firstName,

        @NotNull(message = "Surname is mandatory")
        @Size(max = 50, message = "Surname length should not exceed 50 characters")
        String lastName,

        @Size(max = 20, message = "Phone length should not exceed 50 characters")
        String phone
) {
}

package com.wise.petadoption.shelter.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShelterRequest(
        @NotNull(message = "Shelter name is mandatory")
        @Size(max = 100, message = "Name length should not exceed 100 characters")
        String name,

        @NotNull(message = "City name is mandatory")
        @Size(max = 50, message = "City length should not exceed 50 characters")
        String city,

        String address,

        @Email
        @NotNull(message = "Email is mandatory")
        String contactEmail,

        @Pattern(
                regexp = "^\\+?[1-9]\\d{1,14}$",
                message = "Phone number must be valid"
        )
        String contactPhone,

        String description
) {
}

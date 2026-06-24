package com.wise.petadoption.adoption.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
        @NotNull
        Long animalId,
        @NotBlank
        String message
) {
}

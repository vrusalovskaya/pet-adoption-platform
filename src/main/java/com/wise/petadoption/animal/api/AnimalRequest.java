package com.wise.petadoption.animal.api;

import com.wise.petadoption.animal.common.Gender;
import com.wise.petadoption.animal.common.Species;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnimalRequest(
        @NotNull(message = "Shelter id is mandatory")
        Long shelterId,

        @NotNull(message = "Name is mandatory")
        @Size(max = 50, message = "Name length should not exceed 50 characters")
        String name,

        @NotNull(message = "Species is mandatory")
        Species species,

        @Size(max = 50, message = "Breed length should not exceed 50 characters")
        String breed,

        @Min(2000)
        @Max(2026)
        Integer birthYear,

        @NotNull(message = "Gender is mandatory")
        Gender gender,

        @Size(max = 5000)
        String description) {
}

package com.wise.petadoption.animal.api;

import com.wise.petadoption.animal.common.AnimalStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAnimalStatusRequest(
        @NotNull(message = "Status is mandatory")
        AnimalStatus status
) {
}

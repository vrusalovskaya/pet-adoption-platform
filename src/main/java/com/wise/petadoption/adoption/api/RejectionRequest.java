package com.wise.petadoption.adoption.api;

import jakarta.validation.constraints.NotBlank;

public record RejectionRequest(
        @NotBlank
        String decisionComment
) {
}

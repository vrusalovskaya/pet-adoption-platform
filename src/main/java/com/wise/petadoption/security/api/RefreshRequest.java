package com.wise.petadoption.security.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(
        @Size(max = 64)
        @NotBlank
        String refreshToken
) {
}

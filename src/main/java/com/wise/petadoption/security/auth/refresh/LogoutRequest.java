package com.wise.petadoption.security.auth.refresh;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
        @Size(max = 64)
        @NotBlank
        String refreshToken
) {
}

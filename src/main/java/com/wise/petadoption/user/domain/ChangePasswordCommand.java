package com.wise.petadoption.user.domain;

public record ChangePasswordCommand(
        Long id,
        String oldPassword,
        String newPassword
) {
}

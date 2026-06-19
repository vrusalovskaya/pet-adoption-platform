package com.wise.petadoption.user.domain;

public record ChangePasswordCommand(
        String oldPassword,
        String newPassword
) {
}

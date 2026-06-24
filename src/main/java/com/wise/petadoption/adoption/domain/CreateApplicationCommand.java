package com.wise.petadoption.adoption.domain;

public record CreateApplicationCommand(
        Long animalId,
        Long applicantId,
        String message
) {
}

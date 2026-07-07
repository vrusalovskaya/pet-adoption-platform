package com.wise.petadoption.adoption.domain;

import com.wise.petadoption.adoption.common.ApplicationStatus;

import java.time.Instant;

public record Application(
        Long id,
        Long animalId,
        Long applicantId,
        String message,
        ApplicationStatus status,
        String decisionComment,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.wise.petadoption.adoption.api;

import com.wise.petadoption.adoption.common.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long animalId,
        Long applicantId,
        String message,
        ApplicationStatus status,
        String decisionComment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

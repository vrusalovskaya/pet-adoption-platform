package com.wise.petadoption.shelter.api;

import java.time.LocalDateTime;

public record ShelterResponse (
        Long id,
        String name,
        String city,
        String address,
        String contactEmail,
        String contactPhone,
        String description,
        boolean verified,
        LocalDateTime createdAt
) {
}

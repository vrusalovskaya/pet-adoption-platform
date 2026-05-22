package com.wise.petadoption.shelter.domain;

import java.time.LocalDateTime;

public record Shelter(
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

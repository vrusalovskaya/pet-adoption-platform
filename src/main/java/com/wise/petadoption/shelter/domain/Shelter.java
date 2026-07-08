package com.wise.petadoption.shelter.domain;

import java.time.Instant;

public record Shelter(
        Long id,
        String name,
        String city,
        String address,
        String contactEmail,
        String contactPhone,
        String description,
        boolean verified,
        Instant createdAt
) {
}

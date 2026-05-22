package com.wise.petadoption.shelter.domain;

public record ModifyShelterCommand(
        Long id,
        String name,
        String city,
        String address,
        String contactEmail,
        String contactPhone,
        String description
) {
}

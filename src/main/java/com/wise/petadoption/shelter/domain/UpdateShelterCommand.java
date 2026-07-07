package com.wise.petadoption.shelter.domain;

public record UpdateShelterCommand(
        Long id,
        String name,
        String city,
        String address,
        String contactEmail,
        String contactPhone,
        String description
) {
}

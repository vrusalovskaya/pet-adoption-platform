package com.wise.petadoption.animal.domain;

import com.wise.petadoption.animal.common.Gender;
import com.wise.petadoption.animal.common.Species;

public record CreateAnimalCommand(
        Long shelterId,
        String name,
        Species species,
        String breed,
        Integer birthYear,
        Gender gender,
        String description
) {
}

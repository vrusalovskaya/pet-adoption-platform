package com.wise.petadoption.animal.mapper;

import com.wise.petadoption.animal.api.AnimalResponse;
import com.wise.petadoption.animal.domain.Animal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnimalResponseMapper {
    AnimalResponse toResponse(Animal animal);
}

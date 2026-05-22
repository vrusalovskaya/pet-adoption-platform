package com.wise.petadoption.shelter.mapper;

import com.wise.petadoption.shelter.api.ShelterResponse;
import com.wise.petadoption.shelter.domain.ModifyShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShelterMapper {
    Shelter toModel(ShelterEntity shelterEntity);

    ShelterResponse toResponse(Shelter shelter);

    ShelterEntity toEntity(ModifyShelterCommand shelter);
}

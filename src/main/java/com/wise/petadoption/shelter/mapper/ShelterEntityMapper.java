package com.wise.petadoption.shelter.mapper;

import com.wise.petadoption.shelter.domain.CreateShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShelterEntityMapper {
    Shelter toModel(ShelterEntity shelterEntity);

    ShelterEntity toEntity(CreateShelterCommand shelter);

    ShelterEntity toEntity(UpdateShelterCommand shelter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "verified", ignore = true)
    void updateEntity(UpdateShelterCommand command, @MappingTarget ShelterEntity entity);
}

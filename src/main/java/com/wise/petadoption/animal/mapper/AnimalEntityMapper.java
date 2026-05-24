package com.wise.petadoption.animal.mapper;

import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.ModifyAnimalCommand;
import com.wise.petadoption.animal.persistence.AnimalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnimalEntityMapper {

    AnimalEntity toEntity(ModifyAnimalCommand command);

    @Mapping(target = "shelterId", source = "shelterEntity.id")
    Animal toModel(AnimalEntity animalEntity);
}

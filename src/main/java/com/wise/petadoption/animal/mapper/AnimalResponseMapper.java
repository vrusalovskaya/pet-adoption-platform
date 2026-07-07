package com.wise.petadoption.animal.mapper;

import com.wise.petadoption.animal.api.AnimalResponse;
import com.wise.petadoption.animal.domain.Animal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.net.URI;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnimalResponseMapper {

    @Mapping(target = "photoUrl", expression = "java(buildPhotoUrl(animal))")
    AnimalResponse toResponse(Animal animal);

    default URI buildPhotoUrl(Animal animal) {
        return animal.photoMetadata() == null
                ? null
                : URI.create("/api/v1/animals/" + animal.id() + "/photo");
    }
}

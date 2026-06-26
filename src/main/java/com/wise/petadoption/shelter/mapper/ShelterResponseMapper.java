package com.wise.petadoption.shelter.mapper;

import com.wise.petadoption.shelter.api.ShelterResponse;
import com.wise.petadoption.shelter.domain.Shelter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShelterResponseMapper {
    ShelterResponse toResponse(Shelter shelter);
}

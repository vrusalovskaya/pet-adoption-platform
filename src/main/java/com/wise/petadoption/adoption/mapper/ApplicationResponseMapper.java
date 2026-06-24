package com.wise.petadoption.adoption.mapper;

import com.wise.petadoption.adoption.api.ApplicationResponse;
import com.wise.petadoption.adoption.domain.Application;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApplicationResponseMapper {

    ApplicationResponse toResponse(Application application);
}

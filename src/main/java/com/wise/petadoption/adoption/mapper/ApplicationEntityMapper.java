package com.wise.petadoption.adoption.mapper;

import com.wise.petadoption.adoption.domain.Application;
import com.wise.petadoption.adoption.domain.CreateApplicationCommand;
import com.wise.petadoption.adoption.persistence.ApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApplicationEntityMapper {

    Application toModel(ApplicationEntity entity);

    ApplicationEntity toEntity(CreateApplicationCommand command);
}

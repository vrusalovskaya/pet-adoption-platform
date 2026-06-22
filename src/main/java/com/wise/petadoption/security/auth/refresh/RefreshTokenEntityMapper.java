package com.wise.petadoption.security.auth.refresh;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RefreshTokenEntityMapper {

    @Mapping(target = "userId", source = "userEntity.id")
    RefreshToken toModel(RefreshTokenEntity entity);
}

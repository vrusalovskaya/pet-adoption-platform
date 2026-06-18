package com.wise.petadoption.user;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {

    UserEntity toEntity(CreateUserCommand command);

    User toModel(UserEntity userEntity);
}

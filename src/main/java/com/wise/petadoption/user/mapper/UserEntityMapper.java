package com.wise.petadoption.user.mapper;

import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.pesistence.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {

    UserEntity toEntity(CreateUserCommand command);

    User toModel(UserEntity userEntity);
}

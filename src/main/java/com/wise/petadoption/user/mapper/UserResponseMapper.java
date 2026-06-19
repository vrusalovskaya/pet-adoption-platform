package com.wise.petadoption.user.mapper;

import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.api.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserResponseMapper {
    UserResponse toResponse(User user);
}

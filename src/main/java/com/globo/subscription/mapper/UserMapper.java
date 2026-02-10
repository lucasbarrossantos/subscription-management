package com.globo.subscription.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.globo.subscription.domain.model.UserEntity;
import com.globo.subscription.presentation.dto.user.UserRequest;
import com.globo.subscription.presentation.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    UserEntity toEntity(UserRequest userRequest);
    UserResponse toResponse(UserEntity userEntity);
    List<UserResponse> toResponse(List<UserEntity> userEntity);

}
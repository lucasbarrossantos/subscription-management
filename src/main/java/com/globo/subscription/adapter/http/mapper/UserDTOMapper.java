package com.globo.subscription.adapter.http.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.adapter.http.dto.user.UserRequest;
import com.globo.subscription.adapter.http.dto.user.UserResponse;

import com.globo.subscription.adapter.http.dto.user.UserUpdateRequest;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    @Mapping(target = "id", ignore = true)
    User toDomain(UserRequest userRequest);
        
    @Mapping(target = "id", ignore = true)
    User toDomain(UserUpdateRequest userUpdateRequest);

    UserResponse toResponse(User user);
    
    List<UserResponse> toResponse(List<User> user);

}
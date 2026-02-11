package com.globo.subscription.adapter.http.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.adapter.http.dto.user.UserRequest;
import com.globo.subscription.adapter.http.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    User toDomain(UserRequest userRequest);

    UserResponse toResponse(User user);
    
    List<UserResponse> toResponse(List<User> user);

}
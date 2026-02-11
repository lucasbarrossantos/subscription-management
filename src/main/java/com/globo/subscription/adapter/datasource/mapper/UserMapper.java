package com.globo.subscription.adapter.datasource.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.globo.subscription.adapter.datasource.database.entity.UserEntity;
import com.globo.subscription.core.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserEntity toEntity(User user);
    
    User toDomain(UserEntity userEntity);
    
    List<User> toDomain(List<UserEntity> userEntity);

}
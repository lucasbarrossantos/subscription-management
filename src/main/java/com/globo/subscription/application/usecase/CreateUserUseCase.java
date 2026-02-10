package com.globo.subscription.application.usecase;

import org.springframework.stereotype.Component;

import com.globo.subscription.application.exceptions.EmailAlreadyExistsException;
import com.globo.subscription.domain.model.UserEntity;
import com.globo.subscription.domain.repository.UserRepository;
import com.globo.subscription.mapper.UserMapper;
import com.globo.subscription.presentation.dto.user.UserRequest;
import com.globo.subscription.presentation.dto.user.UserResponse;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CreateUserUseCase {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse execute(UserRequest userRequest) {
        UserEntity userEntity = userMapper.toEntity(userRequest);

        if (userRepository.existsByEmail(userEntity.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + userEntity.getEmail() + " already exists");
        }

        return userMapper.toResponse(userRepository.save(userEntity));
    }
}
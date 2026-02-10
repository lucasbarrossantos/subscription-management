package com.globo.subscription.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.globo.subscription.domain.repository.UserRepository;
import com.globo.subscription.mapper.UserMapper;
import com.globo.subscription.presentation.dto.user.UserResponse;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GetAllUsersUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> execute() {
        return userMapper.toResponse(userRepository.findAll());
    }
}
package com.globo.subscription.core.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.in.user.GetUserPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetUserUseCase implements GetUserPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User execute(UUID id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}

package com.globo.subscription.core.usecase;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.exception.EmailAlreadyExistsException;
import com.globo.subscription.core.port.in.CreateUserPort;
import com.globo.subscription.core.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CreateUserUseCase implements CreateUserPort {
    
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User execute(User user) {
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + user.getEmail() + " already exists");
        }

        return userRepositoryPort.save(user);
    }
}
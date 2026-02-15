package com.globo.subscription.core.usecase.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.exception.EmailAlreadyExistsException;
import com.globo.subscription.core.port.in.user.CreateUserPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Slf4j
@Service
@AllArgsConstructor
public class CreateUserUseCase implements CreateUserPort {
    
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User execute(User user) {
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            log.warn("Email {} já existe! Impossível cadastrar.", user.getEmail());
            throw new EmailAlreadyExistsException("Email " + user.getEmail() + " already exists");
        }

        return userRepositoryPort.save(user);
    }
}
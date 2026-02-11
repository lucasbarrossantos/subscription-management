package com.globo.subscription.core.usecase.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.in.user.DeleteUserPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DeleteUserUseCase implements DeleteUserPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public void execute(UUID id) {
        if (!userRepositoryPort.findById(id).isPresent()) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepositoryPort.delete(id);
    }
}

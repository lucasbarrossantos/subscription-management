package com.globo.subscription.core.usecase.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.exception.EmailAlreadyExistsException;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.in.user.UpdateUserPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UpdateUserUseCase implements UpdateUserPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User execute(UUID id, User user) {
        User existingUser = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (!existingUser.getEmail().equals(user.getEmail()) && userRepositoryPort.existsByEmail(user.getEmail())) {
                throw new EmailAlreadyExistsException("Email " + user.getEmail() + " already exists");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        }

        return userRepositoryPort.save(existingUser);
    }
}

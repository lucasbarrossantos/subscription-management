package com.globo.subscription.core.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.in.GetAllUsersPort;
import com.globo.subscription.core.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetAllUsersUseCase implements GetAllUsersPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public List<User> execute() {
        return userRepositoryPort.findAll();
    }
}
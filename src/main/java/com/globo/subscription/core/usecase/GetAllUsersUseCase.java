package com.globo.subscription.core.usecase;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.port.in.user.GetAllUsersPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GetAllUsersUseCase implements GetAllUsersPort {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public PagedResult<User> execute(int page, int size, String email, String name) {
        return userRepositoryPort.findAll(page, size, email, name);
    }
}
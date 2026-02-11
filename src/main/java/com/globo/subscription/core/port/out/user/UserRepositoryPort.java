package com.globo.subscription.core.port.out.user;

import java.util.Optional;
import java.util.UUID;

import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;

public interface UserRepositoryPort {
    User save(User user);
    boolean existsByEmail(String email);
    PagedResult<User> findAll(int page, int size, String email, String name);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    void delete(UUID id);
}
package com.globo.subscription.core.port.out;

import java.util.List;
import java.util.Optional;
import com.globo.subscription.core.domain.User;

public interface UserRepositoryPort {
    User save(User user);
    boolean existsByEmail(String email);
    List<User> findAll();
    Optional<User> findByEmail(String email);
}
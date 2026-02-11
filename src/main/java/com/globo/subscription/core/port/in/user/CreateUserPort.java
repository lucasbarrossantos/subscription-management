package com.globo.subscription.core.port.in.user;

import com.globo.subscription.core.domain.User;

public interface CreateUserPort {
    User execute(User user);
}
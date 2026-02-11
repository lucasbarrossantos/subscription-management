package com.globo.subscription.core.port.in;

import com.globo.subscription.core.domain.User;

public interface CreateUserPort {
    User execute(User user);
}
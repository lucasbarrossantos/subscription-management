package com.globo.subscription.core.port.in.user;

import java.util.UUID;

import com.globo.subscription.core.domain.User;

public interface GetUserPort {
    User execute(UUID id);
}

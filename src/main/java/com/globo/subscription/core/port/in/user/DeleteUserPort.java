package com.globo.subscription.core.port.in.user;

import java.util.UUID;

public interface DeleteUserPort {
    void execute(UUID id);
}

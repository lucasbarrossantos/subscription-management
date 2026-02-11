package com.globo.subscription.core.port.in.user;

import com.globo.subscription.core.domain.PagedResult;
import com.globo.subscription.core.domain.User;

public interface GetAllUsersPort {
    PagedResult<User> execute(int page, int size, String email, String name);
}
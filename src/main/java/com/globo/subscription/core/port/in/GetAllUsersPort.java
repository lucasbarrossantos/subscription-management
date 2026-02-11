package com.globo.subscription.core.port.in;

import java.util.List;
import com.globo.subscription.core.domain.User;

public interface GetAllUsersPort {
    List<User> execute();
}
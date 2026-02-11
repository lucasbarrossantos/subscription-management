package com.globo.subscription.core.port.in.subscription;

import com.globo.subscription.core.domain.Subscription;

public interface CreateSubscriptionPort {
    Subscription execute(Subscription subscription);
}

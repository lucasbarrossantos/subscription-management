package com.globo.subscription.core.port.in.subscription;

import com.globo.subscription.core.domain.Subscription;

import java.util.List;

public interface RenewSubscriptionsPort {
    List<Subscription> execute();
}

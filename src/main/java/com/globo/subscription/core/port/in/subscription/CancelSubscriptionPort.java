package com.globo.subscription.core.port.in.subscription;

import java.util.UUID;

public interface CancelSubscriptionPort {
    void execute(UUID subscriptionId);
}

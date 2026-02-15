package com.globo.subscription.core.port.in.subscription;

import java.util.UUID;

public interface UpdateSubscriptionStatusPort {
    void execute(UUID subscriptionId, String status);
}

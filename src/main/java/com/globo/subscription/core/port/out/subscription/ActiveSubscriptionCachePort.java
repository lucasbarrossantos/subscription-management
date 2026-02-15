package com.globo.subscription.core.port.out.subscription;

import com.globo.subscription.core.domain.Subscription;
import java.util.Optional;
import java.util.UUID;

public interface ActiveSubscriptionCachePort {
    void putActiveSubscription(UUID userId, Subscription subscription, long ttlSeconds);
    Optional<Subscription> getActiveSubscription(UUID userId);
    void removeActiveSubscription(UUID userId);
}

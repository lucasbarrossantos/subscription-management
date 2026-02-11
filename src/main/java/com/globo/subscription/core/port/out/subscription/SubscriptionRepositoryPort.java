package com.globo.subscription.core.port.out.subscription;

import java.util.Optional;
import java.util.UUID;

import com.globo.subscription.core.domain.Subscription;

public interface SubscriptionRepositoryPort {
    Subscription save(Subscription subscription);
    Optional<Subscription> findActiveByUserId(UUID userId);
    Optional<Subscription> findById(UUID id);
    Optional<Subscription> findLatestByUserId(UUID userId);
}

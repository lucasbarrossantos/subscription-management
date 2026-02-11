package com.globo.subscription.adapter.datasource.database.repository.subscription;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.globo.subscription.adapter.datasource.database.entity.SubscriptionEntity;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {
    Optional<SubscriptionEntity> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
    Optional<SubscriptionEntity> findFirstByUserIdOrderByStartDateDesc(UUID userId);
}

package com.globo.subscription.adapter.datasource.database;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.globo.subscription.adapter.datasource.database.entity.SubscriptionEntity;
import com.globo.subscription.adapter.datasource.database.mapper.SubscriptionMapper;
import com.globo.subscription.adapter.datasource.database.repository.subscription.SubscriptionRepository;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SubscriptionDatabaseAdapter implements SubscriptionRepositoryPort {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity = subscriptionMapper.toEntity(subscription);
        SubscriptionEntity savedEntity = subscriptionRepository.save(entity);
        return subscriptionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Subscription> findActiveByUserId(UUID userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(subscriptionMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return subscriptionRepository.findById(id)
                .map(subscriptionMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findLatestByUserId(UUID userId) {
        return subscriptionRepository.findFirstByUserIdOrderByStartDateDesc(userId)
                .map(subscriptionMapper::toDomain);
    }
}

package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.SubscriptionNotFoundException;
import com.globo.subscription.core.port.in.subscription.UpdateSubscriptionStatusPort;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSubscriptionStatusUseCase implements UpdateSubscriptionStatusPort {

    @Value("${redis.cache.active-subscription-ttl-seconds:3600}")
    private long ttlSeconds;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ActiveSubscriptionCachePort activeSubscriptionCachePort;

    @Override
    public void execute(UUID subscriptionId, String status) {

        var subscription = subscriptionRepositoryPort.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionNotFoundException("Assinatura não encontrada com id: " + subscriptionId));

        SubscriptionStatus newStatus = SubscriptionStatus.valueOf(status.toUpperCase());

        subscription.setStatus(newStatus);
        subscription = subscriptionRepositoryPort.save(subscription);
        activeSubscriptionCachePort.putActiveSubscription(subscription.getUser().getId(), subscription, ttlSeconds);
        log.info("Status da assinatura {} atualizado para {}", subscriptionId, newStatus);
    }
}
package com.globo.subscription.core.usecase.subscription;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.SubscriptionAlreadyCanceledException;
import com.globo.subscription.core.exception.SubscriptionNotFoundException;
import com.globo.subscription.core.port.in.subscription.CancelSubscriptionPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import lombok.AllArgsConstructor;

@Slf4j
@Service
@AllArgsConstructor
public class CancelSubscriptionUseCase implements CancelSubscriptionPort {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final ActiveSubscriptionCachePort activeSubscriptionCachePort;

    @Override
    public void execute(UUID subscriptionId) {
        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Assinatura não encontrada com id: " + subscriptionId));

        if (SubscriptionStatus.CANCELED.equals(subscription.getStatus())) {
            throw new SubscriptionAlreadyCanceledException("Assinatura " + subscriptionId + " já foi cancelada.");
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription = subscriptionRepositoryPort.save(subscription);
        activeSubscriptionCachePort.removeActiveSubscription(subscription.getUser().getId());
        log.info("Assinatura id: {} do cliente: {} foi cancelada com sucesso.", subscriptionId, subscription.getUser().getId());
    }
}

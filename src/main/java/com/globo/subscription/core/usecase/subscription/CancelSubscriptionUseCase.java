package com.globo.subscription.core.usecase.subscription;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.SubscriptionAlreadyCanceledException;
import com.globo.subscription.core.exception.SubscriptionNotFoundException;
import com.globo.subscription.core.port.in.subscription.CancelSubscriptionPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CancelSubscriptionUseCase implements CancelSubscriptionPort {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Override
    public void execute(UUID subscriptionId) {
        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found with id: " + subscriptionId));

        if (SubscriptionStatus.CANCELED.equals(subscription.getStatus())) {
            throw new SubscriptionAlreadyCanceledException("Subscription " + subscriptionId + " is already canceled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepositoryPort.save(subscription);
    }
}

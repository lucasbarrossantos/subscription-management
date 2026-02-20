package com.globo.subscription.core.usecase.subscription.strategy;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoChangePlanChangeStrategy implements PlanChangeStrategy {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Override
    public Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user) {
        log.info("NoChangePlanChangeStrategy.apply - userId={}, subscriptionId={}, plan={}", user.getId(), existingSubscription.getId(), newPlan);

        existingSubscription.setStatus(SubscriptionStatus.PENDING);

        existingSubscription.setPlan(newPlan);
        existingSubscription.setStartDate(LocalDate.now());
        existingSubscription.setExpirationDate(LocalDate.now().plusMonths(1));
        existingSubscription.setUpdatedAt(LocalDateTime.now());
        existingSubscription.setRenewalAttempts(0);

        Subscription saved = subscriptionRepositoryPort.save(existingSubscription);
        log.info("NoChangePlanChangeStrategy.apply - subscription saved id={}, plan={}", saved.getId(), newPlan);
        return saved;
    }
}

package com.globo.subscription.core.usecase.subscription.strategy;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DowngradePlanChangeStrategy implements PlanChangeStrategy {

    private final PaymentPort paymentPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Override
    public Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user) {
        TypePlan oldPlan = existingSubscription.getPlan();
        BigDecimal oldPrice = oldPlan.getPrice();
        BigDecimal newPrice = newPlan.getPrice();
        BigDecimal difference = oldPrice.subtract(newPrice);

        log.info("DowngradePlanChangeStrategy.apply - userId={}, subscriptionId={}, fromPlan={} toPlan={}, refund={}",
            user.getId(), existingSubscription.getId(), oldPlan, newPlan, difference);

        existingSubscription.setStatus(SubscriptionStatus.PENDING);

        paymentPort.creditRefund(user.getId(), difference,
            String.format("Estorno de diferença - Mudança de %s para %s", oldPlan.getDescription(), newPlan.getDescription()),
            existingSubscription.getId());

        log.info("DowngradePlanChangeStrategy.apply - refunded amount={} to user={}", difference, user.getId());

        paymentPort.debitAmount(user.getId(), newPrice,
            String.format("Cobrança do novo plano após downgrade: %s", newPlan.getDescription()),
            existingSubscription.getId());

        existingSubscription.setPlan(newPlan);
        existingSubscription.setStartDate(LocalDate.now());
        existingSubscription.setExpirationDate(LocalDate.now().plusMonths(1));
        existingSubscription.setUpdatedAt(LocalDateTime.now());
        existingSubscription.setRenewalAttempts(0);

        Subscription saved = subscriptionRepositoryPort.save(existingSubscription);
        log.info("DowngradePlanChangeStrategy.apply - subscription saved id={}, newPlan={}", saved.getId(), newPlan);
        return saved;
    }
}

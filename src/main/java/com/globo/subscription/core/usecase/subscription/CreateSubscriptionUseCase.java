package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.exception.ActiveSubscriptionAlreadyExistsException;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.in.subscription.CreateSubscriptionPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CreateSubscriptionUseCase implements CreateSubscriptionPort {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PaymentPort paymentPort;

    @Override
    public Subscription execute(Subscription subscription) {
        User user = userRepositoryPort.findById(subscription.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + subscription.getUser().getId()));

        if (subscriptionRepositoryPort.findActiveByUserId(user.getId()).isPresent()) {
            throw new ActiveSubscriptionAlreadyExistsException("Usuário " + user.getId() + " já possui uma assinatura ativa.");
        }

        Optional<Subscription> latestSubscription = subscriptionRepositoryPort.findLatestByUserId(user.getId());

        if (latestSubscription.isPresent() &&
                SubscriptionStatus.CANCELED.equals(latestSubscription.get().getStatus())) {

            return handlePlanChange(latestSubscription.get(), subscription.getPlan(), user);
        }

        // Nova assinatura - débito completo
        paymentPort.debitSubscriptionPlan(user.getId(), subscription.getPlan());

        subscription.setUser(user);
        subscription.setStartDate(LocalDate.now());
        subscription.setExpirationDate(LocalDate.now().plusMonths(1));
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setRenewalAttempts(0);

        log.info("New subscription created for user {} - plan: {}", user.getId(), subscription.getPlan());
        return subscriptionRepositoryPort.save(subscription);
    }

    private Subscription handlePlanChange(Subscription existingSubscription, TypePlan newPlan, User user) {
        TypePlan oldPlan = existingSubscription.getPlan();
        BigDecimal oldPrice = oldPlan.getPrice();
        BigDecimal newPrice = newPlan.getPrice();

        log.info("Handling plan change for user {} - from {} (R$ {}) to {} (R$ {})",
                user.getId(), oldPlan, oldPrice, newPlan, newPrice);

        int priceComparison = newPrice.compareTo(oldPrice);

        if (priceComparison > 0) {
            // Upgrade: novo plano é mais caro - cobrar apenas a diferença
            BigDecimal difference = newPrice.subtract(oldPrice);
            log.info("Plan upgrade detected - charging only difference of R$ {}", difference);

            paymentPort.debitAmount(user.getId(), difference,
                    String.format("Upgrade de plano: %s para %s (diferença)",
                            oldPlan.getDescription(), newPlan.getDescription()));

        } else if (priceComparison < 0) {
            // Downgrade: novo plano é mais barato - estornar a diferença
            BigDecimal difference = oldPrice.subtract(newPrice);
            log.info("Plan downgrade detected - refunding difference of R$ {}", difference);

            paymentPort.creditRefund(user.getId(), difference,
                    String.format("Estorno de diferença - Mudança de %s para %s",
                            oldPlan.getDescription(), newPlan.getDescription()));

        } else {
            log.info("Plan change with same price - no financial transaction needed");
        }

        existingSubscription.setPlan(newPlan);
        existingSubscription.setStartDate(LocalDate.now());
        existingSubscription.setExpirationDate(LocalDate.now().plusMonths(1));
        existingSubscription.setStatus(SubscriptionStatus.ACTIVE);
        existingSubscription.setUpdatedAt(LocalDateTime.now());
        existingSubscription.setRenewalAttempts(0);

        log.info("Subscription updated for user {} - new plan: {}", user.getId(), newPlan);
        return subscriptionRepositoryPort.save(existingSubscription);
    }
}

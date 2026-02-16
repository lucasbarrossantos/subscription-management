package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.port.in.subscription.RenewSubscriptionsPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class RenewSubscriptionsUseCase implements RenewSubscriptionsPort {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RENEWAL_ATTEMPTS = 3;

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final PaymentPort paymentPort;

    @Override
    public List<Subscription> execute() {

        LocalDate currentDate = LocalDate.now();
        List<Subscription> subscriptionsToRenew = subscriptionRepositoryPort.findSubscriptionsToRenew(currentDate, BATCH_SIZE);

        log.info("Found {} subscriptions to renew", subscriptionsToRenew.size());

        List<Subscription> renewedSubscriptions = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        int suspendedCount = 0;

        for (Subscription subscription : subscriptionsToRenew) {
            try {
                Subscription renewed = renewSubscription(subscription);
                renewedSubscriptions.add(renewed);
                successCount++;
                log.info("Successfully renewed subscription {} for user {}",
                        subscription.getId(), subscription.getUser().getId());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to renew subscription {} for user {} - attempt {}/{}",
                        subscription.getId(),
                        subscription.getUser().getId(),
                        subscription.getRenewalAttempts() + 1,
                        MAX_RENEWAL_ATTEMPTS,
                        e);

                try {
                    handleRenewalFailure(subscription);
                    if (subscription.getStatus() == SubscriptionStatus.SUSPENDED) {
                        suspendedCount++;
                    }
                } catch (Exception ex) {
                    log.error("Error handling renewal failure for subscription {}", subscription.getId(), ex);
                }
            }
        }

        log.info("Renewal process completed - Success: {}, Failures: {}, Suspended: {}",
                successCount, failureCount, suspendedCount);

        return renewedSubscriptions;
    }

    private Subscription renewSubscription(Subscription subscription) {
        log.info("Renewing subscription {} for user {} - plan: {}",
                subscription.getId(),
                subscription.getUser().getId(),
                subscription.getPlan());

        paymentPort.debitAmount(
                subscription.getUser().getId(), 
                subscription.getPlan().getPrice(), 
                String.format("Renovação de %s", subscription.getPlan().getDescription()),
                subscription.getId()
        );

        subscription.setStartDate(LocalDate.now());
        subscription.setExpirationDate(LocalDate.now().plusMonths(1));
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setRenewalAttempts(0);
        subscription.setStatus(SubscriptionStatus.PENDING);

        return subscriptionRepositoryPort.save(subscription);
    }

    private void handleRenewalFailure(Subscription subscription) {
        int currentAttempts = subscription.getRenewalAttempts() != null ? subscription.getRenewalAttempts() : 0;
        int newAttempts = currentAttempts + 1;

        subscription.setRenewalAttempts(newAttempts);
        subscription.setUpdatedAt(LocalDateTime.now());

        if (newAttempts >= MAX_RENEWAL_ATTEMPTS) {
            log.warn("Subscription {} exceeded max renewal attempts ({}) - suspending",
                    subscription.getId(), MAX_RENEWAL_ATTEMPTS);
            subscription.setStatus(SubscriptionStatus.SUSPENDED);
        }

        subscriptionRepositoryPort.save(subscription);
    }
}

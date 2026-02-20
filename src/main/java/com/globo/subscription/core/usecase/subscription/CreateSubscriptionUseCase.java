package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.ActiveSubscriptionAlreadyExistsException;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.exception.WalletNotFoundException;
import com.globo.subscription.core.port.in.subscription.CreateSubscriptionPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import com.globo.subscription.core.port.out.wallet.WalletPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.globo.subscription.core.usecase.subscription.strategy.PlanChangeStrategyResolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSubscriptionUseCase implements CreateSubscriptionPort {

    @Value("${redis.cache.active-subscription-ttl-seconds:3600}")
    private long ttlSeconds;

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PaymentPort paymentPort;
    private final ActiveSubscriptionCachePort activeSubscriptionCachePort;
    private final WalletPort walletPort;
    private final PlanChangeStrategyResolver planChangeStrategyResolver;

    @Override
    public Subscription execute(Subscription subscription) {
        User user = userRepositoryPort.findById(subscription.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + subscription.getUser().getId()));

        if (!walletPort.existsWallet(user.getId())) {
            log.error("Carteira não encontrada para usuário {}", user.getId());
            throw new WalletNotFoundException("Usuário " + user.getId() + " não possui carteira cadastrada.");
        }

        if (subscriptionRepositoryPort.findActiveByUserId(user.getId()).isPresent()) {
            log.error("User {} already has an active subscription", user.getId());
            throw new ActiveSubscriptionAlreadyExistsException("Usuário " + user.getId() + " já possui uma assinatura ativa.");
        }

        Optional<Subscription> latestSubscription = subscriptionRepositoryPort.findLatestByUserId(user.getId());

        if (latestSubscription.isPresent() &&
            SubscriptionStatus.CANCELED.equals(latestSubscription.get().getStatus())) {
            Subscription created = planChangeStrategyResolver
                .resolve(latestSubscription.get(), subscription.getPlan())
                .apply(latestSubscription.get(), subscription.getPlan(), user);
            activeSubscriptionCachePort.putActiveSubscription(user.getId(), created, ttlSeconds);
            return created;
        }

        subscription.setUser(user);
        subscription.setStartDate(LocalDate.now());
        subscription.setExpirationDate(LocalDate.now().plusMonths(1));
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setRenewalAttempts(0);

        log.info("New subscription created for user {} - plan: {}", user.getId(), subscription.getPlan());
        Subscription created = subscriptionRepositoryPort.save(subscription);
        paymentPort.debitSubscriptionPlan(user.getId(), subscription.getPlan(), created.getId());
        activeSubscriptionCachePort.putActiveSubscription(user.getId(), created, ttlSeconds);
        return created;
    }
}

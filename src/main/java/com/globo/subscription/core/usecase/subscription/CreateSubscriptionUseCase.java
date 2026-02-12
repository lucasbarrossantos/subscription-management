package com.globo.subscription.core.usecase.subscription;

import java.time.LocalDate;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.ActiveSubscriptionAlreadyExistsException;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.port.in.subscription.CreateSubscriptionPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import com.globo.subscription.core.port.out.wallet.WalletPort;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CreateSubscriptionUseCase implements CreateSubscriptionPort {

    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final WalletPort walletPort;

    @Override
    public Subscription execute(Subscription subscription) {
        User user = userRepositoryPort.findById(subscription.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + subscription.getUser().getId()));

        if (subscriptionRepositoryPort.findActiveByUserId(user.getId()).isPresent()) {
            throw new ActiveSubscriptionAlreadyExistsException("Usuário " + user.getId() + " já possui uma assinatura ativa.");
        }

        walletPort.debitSubscriptionPlan(user.getId(), subscription.getPlan());

        Optional<Subscription> latestSubscription = subscriptionRepositoryPort.findLatestByUserId(user.getId());

        if (latestSubscription.isPresent()) {
            Subscription existing = latestSubscription.get();
            existing.setPlan(subscription.getPlan());
            existing.setStartDate(LocalDate.now());
            existing.setExpirationDate(LocalDate.now().plusMonths(1));
            existing.setStatus(SubscriptionStatus.ACTIVE);
            return subscriptionRepositoryPort.save(existing);
        }

        subscription.setUser(user);
        subscription.setStartDate(LocalDate.now());
        subscription.setExpirationDate(LocalDate.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return subscriptionRepositoryPort.save(subscription);
    }
}

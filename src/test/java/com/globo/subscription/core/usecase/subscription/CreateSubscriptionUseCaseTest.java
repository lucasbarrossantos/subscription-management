package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.exception.ActiveSubscriptionAlreadyExistsException;
import com.globo.subscription.core.exception.UserNotFoundException;
import com.globo.subscription.core.exception.WalletNotFoundException;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import com.globo.subscription.core.port.out.user.UserRepositoryPort;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import com.globo.subscription.core.port.out.wallet.WalletPort;
import com.globo.subscription.core.usecase.subscription.strategy.PlanChangeStrategy;
import com.globo.subscription.core.usecase.subscription.strategy.PlanChangeStrategyResolver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PaymentPort paymentPort;
    @Mock
    private ActiveSubscriptionCachePort activeSubscriptionCachePort;
    @Mock
    private WalletPort walletPort;
    @Mock
    private PlanChangeStrategyResolver planChangeStrategyResolver;

    @InjectMocks
    private CreateSubscriptionUseCase useCase;

    private User user;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(TypePlan.BASIC);

        // resolver is mocked per-test where needed
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepositoryPort.findById(any())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> useCase.execute(subscription));
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(false);
        assertThrows(WalletNotFoundException.class, () -> useCase.execute(subscription));
    }

    @Test
    void shouldThrowWhenActiveSubscriptionExists() {
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(true);
        when(subscriptionRepositoryPort.findActiveByUserId(any())).thenReturn(Optional.of(new Subscription()));
        assertThrows(ActiveSubscriptionAlreadyExistsException.class, () -> useCase.execute(subscription));
    }

    @Test
    void shouldCreateSubscriptionSuccessfully() {
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(true);
        when(subscriptionRepositoryPort.findActiveByUserId(any())).thenReturn(Optional.empty());
        when(subscriptionRepositoryPort.findLatestByUserId(any())).thenReturn(Optional.empty());
        when(subscriptionRepositoryPort.save(any())).thenReturn(subscription);
        Subscription result = useCase.execute(subscription);
        assertNotNull(result);
        verify(paymentPort).debitSubscriptionPlan(any(), any(), any());
        verify(activeSubscriptionCachePort).putActiveSubscription(any(), any(), anyLong());
    }

    @Test
    void shouldHandlePlanUpgradeAndChargeDifference() {
        Subscription oldSub = new Subscription();
        oldSub.setId(UUID.randomUUID());
        oldSub.setUser(user);
        oldSub.setPlan(TypePlan.BASIC);
        oldSub.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.CANCELED);

        Subscription upgrade = new Subscription();
        upgrade.setUser(user);
        upgrade.setPlan(TypePlan.PREMIUM);

        // mock resolver behavior for plan change
        when(planChangeStrategyResolver.resolve(any(), any())).thenAnswer(invocation -> {
            return new PlanChangeStrategy() {
                @Override
                public Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user) {
                    TypePlan oldPlan = existingSubscription.getPlan();
                    BigDecimal oldPrice = oldPlan.getPrice();
                    BigDecimal newPrice = newPlan.getPrice();
                    BigDecimal difference = newPrice.subtract(oldPrice);
                    paymentPort.debitAmount(user.getId(), difference,
                            String.format("Upgrade de plano: %s para %s (diferença)", oldPlan.getDescription(), newPlan.getDescription()),
                            existingSubscription.getId());
                    existingSubscription.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.PENDING);
                    existingSubscription.setPlan(newPlan);
                    when(subscriptionRepositoryPort.save(any())).thenReturn(existingSubscription);
                    return subscriptionRepositoryPort.save(existingSubscription);
                }
            };
        });
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(true);
        when(subscriptionRepositoryPort.findActiveByUserId(any())).thenReturn(Optional.empty());
        when(subscriptionRepositoryPort.findLatestByUserId(any())).thenReturn(Optional.of(oldSub));
        when(subscriptionRepositoryPort.save(any())).thenReturn(oldSub);

        Subscription result = useCase.execute(upgrade);

        assertNotNull(result);
        verify(paymentPort).debitAmount(eq(user.getId()), eq(TypePlan.PREMIUM.getPrice().subtract(TypePlan.BASIC.getPrice())), contains("Upgrade de plano"), eq(oldSub.getId()));
        verify(activeSubscriptionCachePort).putActiveSubscription(eq(user.getId()), eq(oldSub), anyLong());
    }

    @Test
    void shouldHandlePlanDowngradeAndRefundDifference() {
        Subscription oldSub = new Subscription();
        oldSub.setId(UUID.randomUUID());
        oldSub.setUser(user);
        oldSub.setPlan(TypePlan.PREMIUM);
        oldSub.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.CANCELED);

        Subscription downgrade = new Subscription();
        downgrade.setUser(user);
        downgrade.setPlan(TypePlan.BASIC);

        // mock resolver behavior for plan change
        when(planChangeStrategyResolver.resolve(any(), any())).thenAnswer(invocation -> {
            return new PlanChangeStrategy() {
                @Override
                public Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user) {
                    TypePlan oldPlan = existingSubscription.getPlan();
                    BigDecimal oldPrice = oldPlan.getPrice();
                    BigDecimal newPrice = newPlan.getPrice();
                    BigDecimal difference = oldPrice.subtract(newPrice);
                    paymentPort.creditRefund(user.getId(), difference,
                            String.format("Estorno de diferença - Mudança de %s para %s", oldPlan.getDescription(), newPlan.getDescription()),
                            existingSubscription.getId());
                    paymentPort.debitAmount(user.getId(), newPrice,
                            String.format("Cobrança do novo plano após downgrade: %s", newPlan.getDescription()),
                            existingSubscription.getId());
                    existingSubscription.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.PENDING);
                    existingSubscription.setPlan(newPlan);
                    when(subscriptionRepositoryPort.save(any())).thenReturn(existingSubscription);
                    return subscriptionRepositoryPort.save(existingSubscription);
                }
            };
        });
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(true);
        when(subscriptionRepositoryPort.findActiveByUserId(any())).thenReturn(Optional.empty());
        when(subscriptionRepositoryPort.findLatestByUserId(any())).thenReturn(Optional.of(oldSub));
        when(subscriptionRepositoryPort.save(any())).thenReturn(oldSub);

        Subscription result = useCase.execute(downgrade);

        assertNotNull(result);
        verify(paymentPort).creditRefund(eq(user.getId()), eq(TypePlan.PREMIUM.getPrice().subtract(TypePlan.BASIC.getPrice())), contains("Estorno de diferença"), eq(oldSub.getId()));
        verify(paymentPort).debitAmount(eq(user.getId()), eq(TypePlan.BASIC.getPrice()), contains("Cobrança do novo plano"), eq(oldSub.getId()));
        verify(activeSubscriptionCachePort).putActiveSubscription(eq(user.getId()), eq(oldSub), anyLong());
    }

    @Test
    void shouldHandleReactivationWithoutFinancialTransaction() {
        Subscription oldSub = new Subscription();
        oldSub.setId(UUID.randomUUID());
        oldSub.setUser(user);
        oldSub.setPlan(TypePlan.BASIC);
        oldSub.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.CANCELED);

        Subscription reactivation = new Subscription();
        reactivation.setUser(user);
        reactivation.setPlan(TypePlan.BASIC);

        // mock resolver behavior for reactivation
        when(planChangeStrategyResolver.resolve(any(), any())).thenAnswer(invocation -> {
            return new PlanChangeStrategy() {
                @Override
                public Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user) {
                    existingSubscription.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.ACTIVE);
                    existingSubscription.setPlan(newPlan);
                    when(subscriptionRepositoryPort.save(any())).thenReturn(existingSubscription);
                    return subscriptionRepositoryPort.save(existingSubscription);
                }
            };
        });
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(true);
        when(subscriptionRepositoryPort.findActiveByUserId(any())).thenReturn(Optional.empty());
        when(subscriptionRepositoryPort.findLatestByUserId(any())).thenReturn(Optional.of(oldSub));
        when(subscriptionRepositoryPort.save(any())).thenReturn(oldSub);

        Subscription result = useCase.execute(reactivation);

        assertNotNull(result);
        verify(paymentPort, never()).debitAmount(any(), any(), any(), any());
        verify(paymentPort, never()).creditRefund(any(), any(), any(), any());
        verify(activeSubscriptionCachePort).putActiveSubscription(eq(user.getId()), eq(oldSub), anyLong());
    }

    @Test
    void shouldHandlePlanChangeWithSamePrice() {
        Subscription oldSub = new Subscription();
        oldSub.setId(UUID.randomUUID());
        oldSub.setUser(user);
        oldSub.setPlan(TypePlan.PREMIUM);
        oldSub.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.CANCELED);

        Subscription samePrice = new Subscription();
        samePrice.setUser(user);
        samePrice.setPlan(TypePlan.PREMIUM);

        // mock resolver behavior for same-price change
        when(planChangeStrategyResolver.resolve(any(), any())).thenAnswer(invocation -> {
            return new PlanChangeStrategy() {
                @Override
                public Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user) {
                    existingSubscription.setStatus(com.globo.subscription.core.domain.enums.SubscriptionStatus.PENDING);
                    existingSubscription.setPlan(newPlan);
                    when(subscriptionRepositoryPort.save(any())).thenReturn(existingSubscription);
                    return subscriptionRepositoryPort.save(existingSubscription);
                }
            };
        });
        when(userRepositoryPort.findById(any())).thenReturn(Optional.of(user));
        when(walletPort.existsWallet(any())).thenReturn(true);
        when(subscriptionRepositoryPort.findActiveByUserId(any())).thenReturn(Optional.empty());
        when(subscriptionRepositoryPort.findLatestByUserId(any())).thenReturn(Optional.of(oldSub));
        when(subscriptionRepositoryPort.save(any())).thenReturn(oldSub);

        Subscription result = useCase.execute(samePrice);

        assertNotNull(result);
        verify(paymentPort, never()).debitAmount(any(), any(), any(), any());
        verify(paymentPort, never()).creditRefund(any(), any(), any(), any());
        verify(activeSubscriptionCachePort).putActiveSubscription(eq(user.getId()), eq(oldSub), anyLong());
    }
}

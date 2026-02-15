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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @InjectMocks
    private CreateSubscriptionUseCase useCase;

    private User user;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            user = new User();
            user.setId(UUID.randomUUID());
            subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlan(TypePlan.BASIC);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

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
import static org.mockito.Mockito.*;

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
}

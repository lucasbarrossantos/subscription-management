package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.SubscriptionAlreadyCanceledException;
import com.globo.subscription.core.exception.SubscriptionNotFoundException;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CancelSubscriptionUseCaseTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private ActiveSubscriptionCachePort activeSubscriptionCachePort;
    @InjectMocks
    private CancelSubscriptionUseCase useCase;

    private UUID subscriptionId;
    private Subscription subscription;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionId = UUID.randomUUID();
        user = new User();
        user.setId(UUID.randomUUID());
        subscription = new Subscription();
        subscription.setUser(user);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
    }

    @Test
    void execute_shouldCancelActiveSubscription() {
        when(subscriptionRepositoryPort.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepositoryPort.save(any(Subscription.class))).thenReturn(subscription);
        useCase.execute(subscriptionId);
        verify(subscriptionRepositoryPort).save(subscription);
        verify(activeSubscriptionCachePort).removeActiveSubscription(user.getId());
    }

    @Test
    void execute_shouldThrowIfSubscriptionNotFound() {
        when(subscriptionRepositoryPort.findById(subscriptionId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(subscriptionId))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining(subscriptionId.toString());
        verify(subscriptionRepositoryPort, never()).save(any());
        verify(activeSubscriptionCachePort, never()).removeActiveSubscription(any());
    }

    @Test
    void execute_shouldThrowIfAlreadyCanceled() {
        subscription.setStatus(SubscriptionStatus.CANCELED);
        when(subscriptionRepositoryPort.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        assertThatThrownBy(() -> useCase.execute(subscriptionId))
                .isInstanceOf(SubscriptionAlreadyCanceledException.class)
                .hasMessageContaining(subscriptionId.toString());
        verify(subscriptionRepositoryPort, never()).save(any());
        verify(activeSubscriptionCachePort, never()).removeActiveSubscription(any());
    }
}

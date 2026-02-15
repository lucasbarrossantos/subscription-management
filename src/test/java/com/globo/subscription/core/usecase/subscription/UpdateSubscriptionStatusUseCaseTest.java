package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.SubscriptionNotFoundException;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateSubscriptionStatusUseCaseTest {

    private SubscriptionRepositoryPort repositoryPort;
    @Mock
    private ActiveSubscriptionCachePort activeSubscriptionCachePort;
    private UpdateSubscriptionStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        repositoryPort = mock(SubscriptionRepositoryPort.class);
        activeSubscriptionCachePort = mock(ActiveSubscriptionCachePort.class);
        useCase = new UpdateSubscriptionStatusUseCase(repositoryPort, activeSubscriptionCachePort);
    }

    @Test
    void shouldUpdateStatusToActive() {

        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.PENDING);
        com.globo.subscription.core.domain.User user = new com.globo.subscription.core.domain.User();
        user.setId(UUID.randomUUID());
        subscription.setUser(user);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(subscription));
        when(repositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(id, "ACTIVE");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(repositoryPort).save(captor.capture());
        assertEquals(SubscriptionStatus.ACTIVE, captor.getValue().getStatus());
        verify(activeSubscriptionCachePort).putActiveSubscription(eq(user.getId()), eq(captor.getValue()), anyLong());
    }

    @Test
    void shouldThrowIfSubscriptionNotFound() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());
        assertThrows(SubscriptionNotFoundException.class, () -> useCase.execute(id, "ACTIVE"));
    }

    @Test
    void shouldNotUpdateWhenStatusIsSame() {
        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        com.globo.subscription.core.domain.User user = new com.globo.subscription.core.domain.User();
        user.setId(UUID.randomUUID());
        subscription.setUser(user);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(subscription));
        useCase.execute(id, "ACTIVE");
        verify(repositoryPort, org.mockito.Mockito.never()).save(any());
        verify(activeSubscriptionCachePort, org.mockito.Mockito.never()).putActiveSubscription(any(), any(), anyLong());
    }

    @Test
    void shouldUpdateStatusToSuspended() {
        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.PENDING);
        com.globo.subscription.core.domain.User user = new com.globo.subscription.core.domain.User();
        user.setId(UUID.randomUUID());
        subscription.setUser(user);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(subscription));
        when(repositoryPort.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        useCase.execute(id, "SUSPENDED");
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(repositoryPort).save(captor.capture());
        assertEquals(SubscriptionStatus.SUSPENDED, captor.getValue().getStatus());
        verify(activeSubscriptionCachePort).putActiveSubscription(eq(user.getId()), eq(captor.getValue()), anyLong());
    }
}

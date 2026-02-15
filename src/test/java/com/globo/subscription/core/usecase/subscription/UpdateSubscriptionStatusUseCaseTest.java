package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.exception.SubscriptionNotFoundException;
import com.globo.subscription.core.port.in.subscription.UpdateSubscriptionStatusPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateSubscriptionStatusUseCaseTest {

    private SubscriptionRepositoryPort repositoryPort;
    private UpdateSubscriptionStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        repositoryPort = mock(SubscriptionRepositoryPort.class);
        useCase = new UpdateSubscriptionStatusUseCase(repositoryPort);
    }

    @Test
    void shouldUpdateStatusToActive() {
        UUID id = UUID.randomUUID();
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.PENDING);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(subscription));

        useCase.execute(id, "ACTIVE");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(repositoryPort).save(captor.capture());
        assertEquals(SubscriptionStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void shouldThrowIfSubscriptionNotFound() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());
        assertThrows(SubscriptionNotFoundException.class, () -> useCase.execute(id, "ACTIVE"));
    }
}

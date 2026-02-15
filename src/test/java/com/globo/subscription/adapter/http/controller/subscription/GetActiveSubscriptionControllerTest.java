package com.globo.subscription.adapter.http.controller.subscription;

import com.globo.subscription.adapter.http.dto.ActiveSubscriptionResponse;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GetActiveSubscriptionControllerTest {

    @Mock
    private ActiveSubscriptionCachePort cachePort;
    @InjectMocks
    private GetActiveSubscriptionController controller;

    private UUID userId;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        subscription = new Subscription();
    }

    @Test
    void getActiveSubscription_shouldReturnOkWhenSubscriptionExists() {
        when(cachePort.getActiveSubscription(userId)).thenReturn(Optional.of(subscription));
        ResponseEntity<ActiveSubscriptionResponse> response = controller.getActiveSubscription(userId);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().subscription()).isEqualTo(subscription);
    }

    @Test
    void getActiveSubscription_shouldReturnNotFoundWhenSubscriptionDoesNotExist() {
        when(cachePort.getActiveSubscription(userId)).thenReturn(Optional.empty());
        ResponseEntity<ActiveSubscriptionResponse> response = controller.getActiveSubscription(userId);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }
}

package com.globo.subscription.adapter.http.controller.subscription;

import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRenewalResponse;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionResponse;
import com.globo.subscription.adapter.http.mapper.SubscriptionDTOMapper;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.in.subscription.RenewSubscriptionsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SubscriptionRenewalControllerTest {

    @Mock
    private RenewSubscriptionsPort renewSubscriptionsPort;
    @Mock
    private SubscriptionDTOMapper subscriptionDTOMapper;
    @InjectMocks
    private SubscriptionRenewalController controller;

    private Subscription subscription;
    private SubscriptionResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = mock(Subscription.class);
        response = mock(SubscriptionResponse.class);
    }

    @Test
    void renewal_shouldReturnOkWithRenewedSubscriptions() {
        List<Subscription> renewed = List.of(subscription);
        List<SubscriptionResponse> responses = List.of(response);
        when(renewSubscriptionsPort.execute()).thenReturn(renewed);
        when(subscriptionDTOMapper.toResponse(subscription)).thenReturn(response);

        ResponseEntity<SubscriptionRenewalResponse> result = controller.renewal();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().renewedSubscriptions()).containsExactlyElementsOf(responses);
        assertThat(result.getBody().renewedSubscriptions()).hasSameSizeAs(renewed);

        verify(renewSubscriptionsPort, times(1)).execute();
        verify(subscriptionDTOMapper, times(1)).toResponse(subscription);
    }

    @Test
    void renewal_shouldReturnOkWithEmptyListWhenNoSubscriptionsRenewed() {
        when(renewSubscriptionsPort.execute()).thenReturn(List.of());

        ResponseEntity<SubscriptionRenewalResponse> result = controller.renewal();

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().renewedSubscriptions()).isEmpty();

        verify(renewSubscriptionsPort, times(1)).execute();
    }
}

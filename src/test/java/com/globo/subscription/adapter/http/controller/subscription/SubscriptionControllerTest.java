package com.globo.subscription.adapter.http.controller.subscription;

import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRequest;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionResponse;
import com.globo.subscription.adapter.http.dto.subscription.UpdateSubscriptionStatusRequest;
import com.globo.subscription.adapter.http.mapper.SubscriptionDTOMapper;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.in.subscription.CancelSubscriptionPort;
import com.globo.subscription.core.port.in.subscription.CreateSubscriptionPort;
import com.globo.subscription.core.port.in.subscription.UpdateSubscriptionStatusPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionControllerTest {

    @Mock
    private CreateSubscriptionPort createSubscriptionPort;
    @Mock
    private CancelSubscriptionPort cancelSubscriptionPort;
    @Mock
    private UpdateSubscriptionStatusPort updateSubscriptionStatusPort;
    @Mock
    private SubscriptionDTOMapper subscriptionDTOMapper;
    @InjectMocks
    private SubscriptionController controller;

    private SubscriptionRequest request;
    private Subscription subscription;
    private SubscriptionResponse response;
    private UUID subscriptionId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionId = UUID.randomUUID();
        request = mock(SubscriptionRequest.class);
        subscription = mock(Subscription.class);
        response = mock(SubscriptionResponse.class);
        when(response.id()).thenReturn(subscriptionId);
    }

    @Test
    void create_shouldReturnCreatedWithResponseAndLocation() {
        when(subscriptionDTOMapper.toDomain(request)).thenReturn(subscription);
        when(createSubscriptionPort.execute(subscription)).thenReturn(subscription);
        when(subscriptionDTOMapper.toResponse(subscription)).thenReturn(response);

        ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);

        when(builder.path(anyString())).thenReturn(builder);
        when(builder.buildAndExpand(any(UUID.class))).thenReturn(mock(UriComponents.class));

        UriComponents uriComponents = mock(UriComponents.class);

        when(builder.buildAndExpand(any(UUID.class))).thenReturn(uriComponents);
        when(uriComponents.toUri()).thenReturn(URI.create("/subscriptions/" + subscriptionId));

        try (MockedStatic<ServletUriComponentsBuilder> staticBuilder = mockStatic(ServletUriComponentsBuilder.class)) {
            staticBuilder.when(ServletUriComponentsBuilder::fromCurrentRequest).thenReturn(builder);

            ResponseEntity<SubscriptionResponse> result = controller.create(request);

            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(result.getStatusCode().value()).isEqualTo(201);
            assertThat(result.getBody()).isEqualTo(response);
            assertThat(result.getHeaders().getLocation()).isEqualTo(URI.create("/subscriptions/" + subscriptionId));
        }
    }

    @Test
    void cancel_shouldReturnNoContent() {
        doNothing().when(cancelSubscriptionPort).execute(subscriptionId);

        ResponseEntity<Void> result = controller.cancel(subscriptionId);
        assertThat(result.getStatusCode().value()).isEqualTo(204);

        verify(cancelSubscriptionPort).execute(subscriptionId);
    }

    @Test
    void updateStatus_shouldReturnNoContent() {
        UpdateSubscriptionStatusRequest updateRequest = mock(UpdateSubscriptionStatusRequest.class);

        when(updateRequest.subscriptionId()).thenReturn(subscriptionId);
        when(updateRequest.status()).thenReturn("ACTIVE");

        doNothing().when(updateSubscriptionStatusPort).execute(subscriptionId, "ACTIVE");

        ResponseEntity<Void> result = controller.updateStatus(updateRequest);

        assertThat(result.getStatusCode().value()).isEqualTo(204);
        verify(updateSubscriptionStatusPort).execute(subscriptionId, "ACTIVE");
    }
}

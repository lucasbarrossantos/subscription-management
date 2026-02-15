package com.globo.subscription.adapter.http.controller.subscription;

import java.net.URI;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.globo.subscription.adapter.http.controller.subscription.spec.SubscriptionControllerSpec;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRequest;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionResponse;
import com.globo.subscription.adapter.http.dto.subscription.UpdateSubscriptionStatusRequest;
import com.globo.subscription.adapter.http.mapper.SubscriptionDTOMapper;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.in.subscription.CancelSubscriptionPort;
import com.globo.subscription.core.port.in.subscription.CreateSubscriptionPort;
import com.globo.subscription.core.port.in.subscription.UpdateSubscriptionStatusPort;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/subscriptions")
@AllArgsConstructor
public class SubscriptionController implements SubscriptionControllerSpec {

    private final CreateSubscriptionPort createSubscriptionPort;
    private final CancelSubscriptionPort cancelSubscriptionPort;
    private final UpdateSubscriptionStatusPort updateSubscriptionStatusPort;
    private final SubscriptionDTOMapper subscriptionDTOMapper;

    @Override
    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
        Subscription subscription = subscriptionDTOMapper.toDomain(request);
        Subscription createdSubscription = createSubscriptionPort.execute(subscription);
        SubscriptionResponse response = subscriptionDTOMapper.toResponse(createdSubscription);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        
        return ResponseEntity.created(uri).body(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        cancelSubscriptionPort.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/status")
    public ResponseEntity<Void> updateStatus(@Valid @RequestBody UpdateSubscriptionStatusRequest request) {
        updateSubscriptionStatusPort.execute(request.subscriptionId(), request.status());
        return ResponseEntity.noContent().build();
    }
}
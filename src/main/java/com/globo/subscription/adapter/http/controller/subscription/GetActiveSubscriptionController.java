package com.globo.subscription.adapter.http.controller.subscription;

import com.globo.subscription.adapter.http.dto.ActiveSubscriptionResponse;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.globo.subscription.adapter.http.controller.subscription.spec.GetActiveSubscriptionControllerSpec;

import java.util.UUID;

@RestController
@RequestMapping("/active-subscriptions")
@RequiredArgsConstructor
public class GetActiveSubscriptionController implements GetActiveSubscriptionControllerSpec {

    private final ActiveSubscriptionCachePort cachePort;

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<ActiveSubscriptionResponse> getActiveSubscription(@PathVariable UUID userId) {
        return cachePort.getActiveSubscription(userId)
                .map(sub -> ResponseEntity.ok(new ActiveSubscriptionResponse(sub)))
                .orElse(ResponseEntity.notFound().build());
    }
}

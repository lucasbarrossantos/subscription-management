package com.globo.subscription.adapter.http.controller.subscription;

import com.globo.subscription.adapter.http.dto.subscription.SubscriptionRenewalResponse;
import com.globo.subscription.adapter.http.dto.subscription.SubscriptionResponse;
import com.globo.subscription.adapter.http.mapper.SubscriptionDTOMapper;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.in.subscription.RenewSubscriptionsPort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.globo.subscription.adapter.http.controller.subscription.spec.SubscriptionRenewalControllerSpec;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/subscriptions/renewal")
@AllArgsConstructor
public class SubscriptionRenewalController implements SubscriptionRenewalControllerSpec {

    private final RenewSubscriptionsPort renewSubscriptionsPort;
    private final SubscriptionDTOMapper subscriptionDTOMapper;

    @Override
    @PostMapping
    public ResponseEntity<SubscriptionRenewalResponse> renewal() {
        log.info("Starting subscription renewal process");

        List<Subscription> renewedSubscriptions = renewSubscriptionsPort.execute();

        List<SubscriptionResponse> responses = renewedSubscriptions.stream()
                .map(subscriptionDTOMapper::toResponse)
                .toList();

        SubscriptionRenewalResponse response = new SubscriptionRenewalResponse(
                renewedSubscriptions.size(),
                renewedSubscriptions.size(),
                responses
        );

        log.info("Renewal process completed - {} subscriptions renewed", renewedSubscriptions.size());
        return ResponseEntity.ok(response);
    }
}

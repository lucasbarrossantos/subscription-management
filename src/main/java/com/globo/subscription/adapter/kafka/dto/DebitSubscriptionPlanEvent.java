package com.globo.subscription.adapter.kafka.dto;

import com.globo.subscription.core.domain.enums.TypePlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitSubscriptionPlanEvent {
    private UUID userId;
    private TypePlan plan;
    private String description;
    private UUID subscriptionId;
}

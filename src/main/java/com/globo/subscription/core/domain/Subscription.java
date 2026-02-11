package com.globo.subscription.core.domain;

import java.time.LocalDate;
import java.util.UUID;

import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.domain.enums.TypePlan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {
    
    private UUID id;
    private User user;
    private TypePlan plan;
    private LocalDate startDate;
    private LocalDate expirationDate;
    private SubscriptionStatus status;
}
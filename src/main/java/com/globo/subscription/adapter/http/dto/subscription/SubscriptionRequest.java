package com.globo.subscription.adapter.http.dto.subscription;

import java.util.UUID;

import com.globo.subscription.core.domain.enums.TypePlan;

import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
    @NotNull(message = "{subscription.userId.required}")
    UUID usuarioId,
    @NotNull(message = "{subscription.plan.required}")
    TypePlan plano
) {}

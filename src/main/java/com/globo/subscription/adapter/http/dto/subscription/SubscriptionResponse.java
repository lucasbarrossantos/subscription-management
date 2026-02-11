package com.globo.subscription.adapter.http.dto.subscription;

import java.time.LocalDate;
import java.util.UUID;

import com.globo.subscription.core.domain.enums.TypePlan;

public record SubscriptionResponse(
    UUID id,
    UUID usuarioId,
    TypePlan plano,
    LocalDate dataInicio,
    LocalDate dataExpiracao,
    String status
) {}

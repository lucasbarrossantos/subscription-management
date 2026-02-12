package com.globo.subscription.core.port.out.wallet;

import com.globo.subscription.core.domain.enums.TypePlan;

import java.util.UUID;

public interface WalletPort {
    void debitSubscriptionPlan(UUID userId, TypePlan plan);
}

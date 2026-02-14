package com.globo.subscription.core.port.out.payment;

import com.globo.subscription.core.domain.enums.TypePlan;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentPort {

    void debitSubscriptionPlan(UUID userId, TypePlan plan);

    void debitAmount(UUID userId, BigDecimal amount, String description);

    void creditRefund(UUID userId, BigDecimal amount, String description);
}
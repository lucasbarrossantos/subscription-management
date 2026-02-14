package com.globo.subscription.adapter.kafka;

import com.globo.subscription.adapter.kafka.dto.CreditRefundEvent;
import com.globo.subscription.adapter.kafka.dto.DebitAmountEvent;
import com.globo.subscription.adapter.kafka.dto.DebitSubscriptionPlanEvent;
import com.globo.subscription.adapter.kafka.producer.PaymentEventProducer;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentKafkaAdapter implements PaymentPort {

    private final PaymentEventProducer paymentEventProducer;

    @Override
    public void debitSubscriptionPlan(UUID userId, TypePlan plan) {
        log.info("Publishing debit subscription plan event - userId: {}, plan: {}", userId, plan);

        DebitSubscriptionPlanEvent event = DebitSubscriptionPlanEvent.builder()
                .userId(userId)
                .plan(plan)
                .description("Compra de " + plan.getDescription())
                .build();

        paymentEventProducer.sendDebitSubscriptionPlanEvent(event);

        log.info("Debit subscription plan event published - userId: {}, plan: {}", userId, plan);
    }

    @Override
    public void debitAmount(UUID userId, BigDecimal amount, String description) {
        log.info("Publishing debit amount event - userId: {}, amount: {}, description: {}",
                userId, amount, description);

        DebitAmountEvent event = DebitAmountEvent.builder()
                .userId(userId)
                .amount(amount)
                .description(description)
                .build();

        paymentEventProducer.sendDebitAmountEvent(event);

        log.info("Debit amount event published - userId: {}, amount: {}", userId, amount);
    }

    @Override
    public void creditRefund(UUID userId, BigDecimal amount, String description) {
        log.info("Publishing credit refund event - userId: {}, amount: {}, description: {}",
                userId, amount, description);

        CreditRefundEvent event = CreditRefundEvent.builder()
                .userId(userId)
                .amount(amount)
                .description(description)
                .build();

        paymentEventProducer.sendCreditRefundEvent(event);

        log.info("Credit refund event published - userId: {}, amount: {}", userId, amount);
    }
}

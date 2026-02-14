package com.globo.subscription.adapter.kafka.producer;

import com.globo.subscription.adapter.kafka.dto.CreditRefundEvent;
import com.globo.subscription.adapter.kafka.dto.DebitAmountEvent;
import com.globo.subscription.adapter.kafka.dto.DebitSubscriptionPlanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.payment-debit-subscription-plan}")
    private String debitSubscriptionPlanTopic;

    @Value("${kafka.topics.payment-debit-amount}")
    private String debitAmountTopic;

    @Value("${kafka.topics.payment-credit-refund}")
    private String creditRefundTopic;

    public void sendDebitSubscriptionPlanEvent(DebitSubscriptionPlanEvent event) {
        String key = event.getUserId().toString();

        log.info("Sending debit subscription plan event to Kafka - userId: {}, plan: {}",
                event.getUserId(), event.getPlan());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(debitSubscriptionPlanTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send debit subscription plan event to Kafka - userId: {}, plan: {}",
                        event.getUserId(), event.getPlan(), ex);
            } else {
                log.info("Debit subscription plan event sent successfully - userId: {}, plan: {}, offset: {}",
                        event.getUserId(), event.getPlan(), result.getRecordMetadata().offset());
            }
        });
    }

    public void sendDebitAmountEvent(DebitAmountEvent event) {
        String key = event.getUserId().toString();

        log.info("Sending debit amount event to Kafka - userId: {}, amount: {}",
                event.getUserId(), event.getAmount());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(debitAmountTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send debit amount event to Kafka - userId: {}, amount: {}",
                        event.getUserId(), event.getAmount(), ex);
            } else {
                log.info("Debit amount event sent successfully - userId: {}, amount: {}, offset: {}",
                        event.getUserId(), event.getAmount(), result.getRecordMetadata().offset());
            }
        });
    }

    public void sendCreditRefundEvent(CreditRefundEvent event) {
        String key = event.getUserId().toString();

        log.info("Sending credit refund event to Kafka - userId: {}, amount: {}",
                event.getUserId(), event.getAmount());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(creditRefundTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send credit refund event to Kafka - userId: {}, amount: {}",
                        event.getUserId(), event.getAmount(), ex);
            } else {
                log.info("Credit refund event sent successfully - userId: {}, amount: {}, offset: {}",
                        event.getUserId(), event.getAmount(), result.getRecordMetadata().offset());
            }
        });
    }
}

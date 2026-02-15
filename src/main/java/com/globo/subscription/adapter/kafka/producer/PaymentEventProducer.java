package com.globo.subscription.adapter.kafka.producer;

import com.globo.subscription.adapter.kafka.dto.CreditRefundEvent;
import com.globo.subscription.adapter.kafka.dto.DebitAmountEvent;
import com.globo.subscription.adapter.kafka.dto.DebitSubscriptionPlanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import com.globo.subscription.adapter.kafka.util.TraceUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TRACE_ID_HEADER = "traceId";

    @Value("${kafka.topics.payment-debit-subscription-plan}")
    private String debitSubscriptionPlanTopic;

    @Value("${kafka.topics.payment-debit-amount}")
    private String debitAmountTopic;

    @Value("${kafka.topics.payment-credit-refund}")
    private String creditRefundTopic;

    public void sendDebitSubscriptionPlanEvent(DebitSubscriptionPlanEvent event) {
        String key = event.getUserId().toString();
        String traceId = TraceUtil.getCurrentTraceId();

        log.info("Sending debit subscription plan event to Kafka - userId: {}, plan: {}",
                event.getUserId(), event.getPlan());

        ProducerRecord<String, Object> record = new ProducerRecord<>(debitSubscriptionPlanTopic, key, event);
        if (traceId != null) {
            record.headers().add(TRACE_ID_HEADER, traceId.getBytes());
        }

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);

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
        String traceId = TraceUtil.getCurrentTraceId();

        log.info("Sending debit amount event to Kafka - userId: {}, amount: {}",
                event.getUserId(), event.getAmount());

        ProducerRecord<String, Object> record = new ProducerRecord<>(debitAmountTopic, key, event);
        if (traceId != null) {
            record.headers().add(TRACE_ID_HEADER, traceId.getBytes());
        }

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(record);

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
        String traceId = TraceUtil.getCurrentTraceId();

        log.info("Sending credit refund event to Kafka - userId: {}, amount: {}",
                event.getUserId(), event.getAmount());

        ProducerRecord<String, Object> record = new ProducerRecord<>(creditRefundTopic, key, event);
        if (traceId != null) {
            record.headers().add(TRACE_ID_HEADER, traceId.getBytes());
        }

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(record);

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

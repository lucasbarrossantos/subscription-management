package com.globo.subscription.adapter.kafka.producer;

import com.globo.subscription.adapter.kafka.dto.CreditRefundEvent;
import com.globo.subscription.adapter.kafka.dto.DebitAmountEvent;
import com.globo.subscription.adapter.kafka.dto.DebitSubscriptionPlanEvent;
import com.globo.subscription.adapter.kafka.util.TraceUtil;
import com.globo.subscription.core.domain.enums.TypePlan;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;

class PaymentEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks
    private PaymentEventProducer producer;

    @Mock
    private CompletableFuture<SendResult<String, Object>> future;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        setPrivateField(producer, "debitSubscriptionPlanTopic", "test-debit-subscription-plan");
        setPrivateField(producer, "debitAmountTopic", "test-debit-amount");
        setPrivateField(producer, "creditRefundTopic", "test-credit-refund");
    }

    @Test
    void sendDebitSubscriptionPlanEvent_shouldSendEventWithTraceId() {
        DebitSubscriptionPlanEvent event = mock(DebitSubscriptionPlanEvent.class);

        when(event.getUserId()).thenReturn(userId);
        when(event.getPlan()).thenReturn(TypePlan.BASIC);

        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {

            traceUtilMock.when(TraceUtil::getCurrentTraceId).thenReturn("trace-123");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            producer.sendDebitSubscriptionPlanEvent(event);

            ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);

            verify(kafkaTemplate).send(captor.capture());
            ProducerRecord record = captor.getValue();

            assertThat(record.headers().lastHeader("traceId")).isNotNull();
            assertThat(new String(record.headers().lastHeader("traceId").value())).isEqualTo("trace-123");
        }
    }

    @Test
    void sendDebitAmountEvent_shouldSendEventWithTraceId() {

        DebitAmountEvent event = mock(DebitAmountEvent.class);

        when(event.getUserId()).thenReturn(userId);
        when(event.getAmount()).thenReturn(java.math.BigDecimal.TEN);

        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            traceUtilMock.when(TraceUtil::getCurrentTraceId).thenReturn("trace-456");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            producer.sendDebitAmountEvent(event);

            ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);

            verify(kafkaTemplate).send(captor.capture());
            ProducerRecord record = captor.getValue();

            assertThat(record.headers().lastHeader("traceId")).isNotNull();
            assertThat(new String(record.headers().lastHeader("traceId").value())).isEqualTo("trace-456");
        }
    }

    @Test
    void sendCreditRefundEvent_shouldSendEventWithTraceId() {
        CreditRefundEvent event = mock(CreditRefundEvent.class);

        when(event.getUserId()).thenReturn(userId);
        when(event.getAmount()).thenReturn(java.math.BigDecimal.ONE);

        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {

            traceUtilMock.when(TraceUtil::getCurrentTraceId).thenReturn("trace-789");
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            producer.sendCreditRefundEvent(event);

            ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);
            verify(kafkaTemplate).send(captor.capture());

            ProducerRecord record = captor.getValue();

            assertThat(record.headers().lastHeader("traceId")).isNotNull();
            assertThat(new String(record.headers().lastHeader("traceId").value())).isEqualTo("trace-789");
        }
    }

    @Test
    void sendDebitSubscriptionPlanEvent_shouldNotAddTraceIdIfNull() {
        DebitSubscriptionPlanEvent event = mock(DebitSubscriptionPlanEvent.class);

        when(event.getUserId()).thenReturn(userId);
        when(event.getPlan()).thenReturn(TypePlan.BASIC);

        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            traceUtilMock.when(TraceUtil::getCurrentTraceId).thenReturn(null);
            when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

            producer.sendDebitSubscriptionPlanEvent(event);

            ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);

            verify(kafkaTemplate).send(captor.capture());

            ProducerRecord record = captor.getValue();
            assertThat(record.headers().lastHeader("traceId")).isNull();
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

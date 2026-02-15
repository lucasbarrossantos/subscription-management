package com.globo.subscription.adapter.kafka;

import com.globo.subscription.adapter.kafka.dto.CreditRefundEvent;
import com.globo.subscription.adapter.kafka.dto.DebitAmountEvent;
import com.globo.subscription.adapter.kafka.dto.DebitSubscriptionPlanEvent;
import com.globo.subscription.adapter.kafka.producer.PaymentEventProducer;
import com.globo.subscription.core.domain.enums.TypePlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PaymentKafkaAdapterTest {

    @Mock
    private PaymentEventProducer paymentEventProducer;
    @InjectMocks
    private PaymentKafkaAdapter adapter;

    private UUID userId;
    private UUID subscriptionId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();
    }

    @Test
    void debitSubscriptionPlan_shouldPublishEventWithCorrectData() {

        adapter.debitSubscriptionPlan(userId, TypePlan.BASIC, subscriptionId);
        ArgumentCaptor<DebitSubscriptionPlanEvent> captor = ArgumentCaptor.forClass(DebitSubscriptionPlanEvent.class);

        verify(paymentEventProducer).sendDebitSubscriptionPlanEvent(captor.capture());

        DebitSubscriptionPlanEvent event = captor.getValue();

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getPlan()).isEqualTo(TypePlan.BASIC);
        assertThat(event.getDescription()).contains("Compra de");
        assertThat(event.getSubscriptionId()).isEqualTo(subscriptionId);
    }

    @Test
    void debitAmount_shouldPublishEventWithCorrectData() {
        BigDecimal amount = new BigDecimal("39.90");
        String description = "Debito de upgrade";

        adapter.debitAmount(userId, amount, description, subscriptionId);
        ArgumentCaptor<DebitAmountEvent> captor = ArgumentCaptor.forClass(DebitAmountEvent.class);

        verify(paymentEventProducer).sendDebitAmountEvent(captor.capture());

        DebitAmountEvent event = captor.getValue();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getAmount()).isEqualTo(amount);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getSubscriptionId()).isEqualTo(subscriptionId);
    }

    @Test
    void creditRefund_shouldPublishEventWithCorrectData() {
        BigDecimal amount = new BigDecimal("10.00");
        String description = "Estorno de downgrade";
        adapter.creditRefund(userId, amount, description, subscriptionId);

        ArgumentCaptor<CreditRefundEvent> captor = ArgumentCaptor.forClass(CreditRefundEvent.class);
        verify(paymentEventProducer).sendCreditRefundEvent(captor.capture());

        CreditRefundEvent event = captor.getValue();

        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getAmount()).isEqualTo(amount);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getSubscriptionId()).isEqualTo(subscriptionId);
    }
}

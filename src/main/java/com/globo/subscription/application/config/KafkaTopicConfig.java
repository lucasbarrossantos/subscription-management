package com.globo.subscription.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topics.payment-debit-subscription-plan}")
    private String debitSubscriptionPlanTopic;

    @Value("${kafka.topics.payment-debit-amount}")
    private String debitAmountTopic;

    @Value("${kafka.topics.payment-credit-refund}")
    private String creditRefundTopic;

    @Bean
    public NewTopic paymentDebitSubscriptionPlanTopic() {
        return TopicBuilder.name(debitSubscriptionPlanTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentDebitAmountTopic() {
        return TopicBuilder.name(debitAmountTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCreditRefundTopic() {
        return TopicBuilder.name(creditRefundTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

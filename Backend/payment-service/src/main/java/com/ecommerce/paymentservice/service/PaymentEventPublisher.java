package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, PaymentCompletedEvent>> future =
                kafkaTemplate.send(TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent PaymentCompletedEvent for orderId=[{}] with offset=[{}]",
                        key, result.getRecordMetadata().offset());
            } else {
                log.error("Unable to send PaymentCompletedEvent for orderId=[{}] due to: {}",
                        key, ex.getMessage(), ex);
            }
        });
    }
}
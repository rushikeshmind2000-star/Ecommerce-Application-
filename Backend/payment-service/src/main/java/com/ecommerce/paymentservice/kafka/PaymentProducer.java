package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.dto.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        LOGGER.info(String.format("Payment processed event published => %s", event.toString()));
        
        Message<PaymentProcessedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "payment_processed_topic")
                .build();
                
        kafkaTemplate.send(message);
    }
}

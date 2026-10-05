package com.example.orderservice.kafka;

import com.example.orderservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        LOGGER.info(String.format("Order event published => %s", event.toString()));
        
        Message<OrderCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "order_created_topic")
                .build();
                
        kafkaTemplate.send(message);
    }

    public void publishOrderCancelled(com.example.orderservice.dto.OrderCancelledEvent event) {
        LOGGER.info(String.format("Order cancelled event published => %s", event.toString()));

        Message<com.example.orderservice.dto.OrderCancelledEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "order_cancelled_topic")
                .build();

        kafkaTemplate.send(message);
    }
}


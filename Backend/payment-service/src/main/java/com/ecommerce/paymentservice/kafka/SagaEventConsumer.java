package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.dto.OrderCancelledEvent;
import com.ecommerce.paymentservice.dto.OrderCreatedEvent;
import com.ecommerce.paymentservice.dto.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SagaEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SagaEventConsumer.class);
    private final PaymentProducer paymentProducer;

    public SagaEventConsumer(PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
    }

    @KafkaListener(topics = "order_created_topic", groupId = "payment-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        LOGGER.info(String.format("Order event received in Payment Service => %s", event.toString()));
        
        // Simulate payment processing logic
        boolean isSuccess = true; // In a real scenario, call payment gateway
        
        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(event.getOrderId(), isSuccess);
        paymentProducer.publishPaymentProcessed(paymentEvent);
    }

    @KafkaListener(topics = "order_cancelled_topic", groupId = "payment-group")
    public void consumeOrderCancelledEvent(OrderCancelledEvent event) {
        LOGGER.info(String.format("Order cancelled event received in Payment Service => %s", event.toString()));
        // Simulate payment refund logic
        LOGGER.info("Payment refunded for order: {}", event.getOrderId());
    }
}

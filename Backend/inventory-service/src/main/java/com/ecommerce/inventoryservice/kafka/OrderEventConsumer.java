package com.ecommerce.inventoryservice.kafka;

import com.ecommerce.inventoryservice.dto.InventoryReservedEvent;
import com.ecommerce.inventoryservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate;

    public OrderEventConsumer(KafkaTemplate<String, InventoryReservedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order_created_topic", groupId = "inventory-group")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        LOGGER.info(String.format("Order event received in Inventory Service => %s", event.toString()));
        
        // Simulate inventory reservation logic
        boolean isReserved = true; // In a real scenario, this checks DB/Cache
        
        InventoryReservedEvent inventoryEvent = new InventoryReservedEvent(event.getOrderId(), isReserved);
        publishInventoryReserved(inventoryEvent);
    }
    
    public void publishInventoryReserved(InventoryReservedEvent event) {
        LOGGER.info(String.format("Inventory reserved event published => %s", event.toString()));
        
        Message<InventoryReservedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "inventory_reserved_topic")
                .build();
                
        kafkaTemplate.send(message);
    }

    @KafkaListener(topics = "order_cancelled_topic", groupId = "inventory-group")
    public void consumeOrderCancelledEvent(com.ecommerce.inventoryservice.dto.OrderCancelledEvent event) {
        LOGGER.info(String.format("Order cancelled event received in Inventory Service => %s", event.toString()));
        // Simulate inventory release logic
        // boolean released = releaseInventory(event.getOrderId());
        LOGGER.info("Inventory released for order: {}", event.getOrderId());
    }
}

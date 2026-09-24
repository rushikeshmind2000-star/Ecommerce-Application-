package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderCreatedEvent;
import com.example.orderservice.kafka.OrderProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders/test")
public class OrderTestKafkaController {

    private final OrderProducer orderProducer;

    public OrderTestKafkaController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publishTestEvent(@RequestBody OrderCreatedEvent event) {
        if (event.getOrderId() == null) {
            event.setOrderId(UUID.randomUUID());
        }
        if (event.getProductId() == null) {
            event.setProductId(UUID.randomUUID());
        }
        
        orderProducer.publishOrderCreated(event);
        return ResponseEntity.ok("OrderCreatedEvent published successfully to Kafka!");
    }
}

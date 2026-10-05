package com.example.orderservice.kafka;

import com.example.orderservice.dto.InventoryReservedEvent;
import com.example.orderservice.dto.OrderCancelledEvent;
import com.example.orderservice.dto.PaymentProcessedEvent;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.OrderStatusHistory;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderProducer orderProducer;

    @KafkaListener(topics = "inventory_reserved_topic", groupId = "order-saga-group")
    @Transactional
    public void consumeInventoryReservedEvent(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for Order: {}", event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) return;

        if (event.isReserved()) {
            order.setInventoryStatus("RESERVED");
            checkSagaCompletion(order);
        } else {
            order.setInventoryStatus("FAILED");
            cancelOrder(order, "Inventory reservation failed");
        }
    }

    @KafkaListener(topics = "payment_processed_topic", groupId = "order-saga-group")
    @Transactional
    public void consumePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent for Order: {}", event.getOrderId());
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) return;

        if (event.isSuccess()) {
            order.setPaymentStatus("PROCESSED");
            checkSagaCompletion(order);
        } else {
            order.setPaymentStatus("FAILED");
            cancelOrder(order, "Payment failed");
        }
    }

    private void checkSagaCompletion(Order order) {
        if ("RESERVED".equals(order.getInventoryStatus()) && "PROCESSED".equals(order.getPaymentStatus())) {
            updateStatus(order, OrderStatus.CONFIRMED, "Saga completed successfully");
        } else if (order.getStatus() == OrderStatus.PENDING) {
            updateStatus(order, OrderStatus.PROCESSING_SAGA, "Saga in progress");
        }
    }

    private void cancelOrder(Order order, String reason) {
        if (order.getStatus() != OrderStatus.CANCELLED) {
            updateStatus(order, OrderStatus.CANCELLED, reason);
            orderProducer.publishOrderCancelled(new OrderCancelledEvent(order.getId(), reason));
        }
    }

    private void updateStatus(Order order, OrderStatus newStatus, String reason) {
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) return;

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .build();
        historyRepository.save(history);
    }
}

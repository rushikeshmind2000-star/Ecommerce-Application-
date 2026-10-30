package com.ecommerce.notificationservice.consumer;

import com.ecommerce.notificationservice.domain.Notification;
import com.ecommerce.notificationservice.domain.NotificationChannel;
import com.ecommerce.notificationservice.domain.NotificationStatus;
import com.ecommerce.notificationservice.event.OrderPlacedEvent;
import com.ecommerce.notificationservice.event.PaymentCompletedEvent;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);
    private final NotificationRepository repository;

    public NotificationKafkaListener(NotificationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for orderId: {}", event.orderId());

        Notification notification = new Notification();
        notification.setUserId(event.userId());
        notification.setOrderId(event.orderId());
        notification.setRecipient(event.customerEmail());
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setSubject("Payment Received for Order #" + event.orderId());
        notification.setContent(String.format("Your payment of %s %s was successful. Reference ID: %s",
                event.amount(), event.currency(), event.paymentId()));
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        repository.save(notification);
        log.info("Persisted email notification for payment ID: {}", event.paymentId());
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for orderId: {}", event.orderId());

        Notification notification = new Notification();
        notification.setUserId(event.userId());
        notification.setOrderId(event.orderId());
        notification.setRecipient(event.customerEmail());
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setSubject("Order Confirmed #" + event.orderId());
        notification.setContent(String.format("Thank you! Your order has been placed successfully for total amount: %s",
                event.totalAmount()));
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        repository.save(notification);
        log.info("Persisted confirmation notification for order ID: {}", event.orderId());
    }
}
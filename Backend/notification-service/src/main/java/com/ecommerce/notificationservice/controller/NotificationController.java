package com.ecommerce.notificationservice.controller;

import com.ecommerce.notificationservice.domain.Notification;
import com.ecommerce.notificationservice.dto.NotificationResponse;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(@PathVariable UUID userId) {
        List<NotificationResponse> list = repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByOrderId(@PathVariable UUID orderId) {
        List<NotificationResponse> list = repository.findByOrderId(orderId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getOrderId(),
                n.getRecipient(),
                n.getChannel(),
                n.getSubject(),
                n.getContent(),
                n.getStatus(),
                n.getSentAt(),
                n.getCreatedAt()
        );
    }
}
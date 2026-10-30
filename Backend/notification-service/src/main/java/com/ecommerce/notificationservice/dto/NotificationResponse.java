package com.ecommerce.notificationservice.dto;

import com.ecommerce.notificationservice.domain.NotificationChannel;
import com.ecommerce.notificationservice.domain.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        UUID orderId,
        String recipient,
        NotificationChannel channel,
        String subject,
        String content,
        NotificationStatus status,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {}
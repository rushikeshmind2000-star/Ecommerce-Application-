package com.ecommerce.notificationservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        UUID userId,
        String customerEmail,
        BigDecimal totalAmount
) {}
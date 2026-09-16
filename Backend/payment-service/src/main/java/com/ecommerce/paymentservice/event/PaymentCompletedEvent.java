package com.ecommerce.paymentservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID paymentId,
        UUID orderId,
        UUID userId,
        String customerEmail,
        BigDecimal amount,
        String currency,
        String status
) {}
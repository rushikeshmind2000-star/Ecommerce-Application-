package com.ecommerce.commondto.payment;

import com.ecommerce.commondto.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response record representing the details of a payment transaction.
 */
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        String paymentReference,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

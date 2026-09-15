package com.ecommerce.paymentservice.dto;



import com.ecommerce.paymentservice.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
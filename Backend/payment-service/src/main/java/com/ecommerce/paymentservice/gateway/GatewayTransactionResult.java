package com.ecommerce.paymentservice.gateway;

import com.ecommerce.paymentservice.domain.PaymentStatus;

public record GatewayTransactionResult(
        boolean successful,
        String gatewayTransactionId,
        PaymentStatus status,
        String failureReason
) {}
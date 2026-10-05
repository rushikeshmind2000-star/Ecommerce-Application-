package com.ecommerce.commondto.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * Request record to initiate a refund for an existing payment.
 */
public record RefundRequest(
        @NotBlank(message = "Refund reason is mandatory")
        String reason
) {}

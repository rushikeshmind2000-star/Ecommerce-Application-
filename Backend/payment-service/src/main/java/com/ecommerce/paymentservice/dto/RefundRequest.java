package com.ecommerce.paymentservice.dto;



import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank(message = "Refund reason is mandatory")
        String reason
) {}
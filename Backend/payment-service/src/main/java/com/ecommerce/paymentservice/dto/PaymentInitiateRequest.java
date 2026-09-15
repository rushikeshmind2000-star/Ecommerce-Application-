package com.ecommerce.paymentservice.dto;



import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiateRequest(
        @NotNull(message = "Order ID is mandatory")
        UUID orderId,

        @NotNull(message = "User ID is mandatory")
        UUID userId,

        @NotNull(message = "Amount is mandatory")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is mandatory")
        String currency,

        @NotBlank(message = "Payment method is mandatory")
        String paymentMethod
) {}

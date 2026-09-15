package com.example.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing a single order line-item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private UUID id;
    private UUID orderId;
    private UUID productId;
    private String productName;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}

package com.ecommerce.commondto.order;

import com.ecommerce.commondto.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO representing a full order with its line-items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private UUID id;
    private UUID userId;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private String shippingAddress;
    private String billingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Line-items belonging to this order. */
    private List<OrderItemDTO> items;
}

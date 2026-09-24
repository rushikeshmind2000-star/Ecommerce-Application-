package com.ecommerce.commondto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO to reserve stock for an order item.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}

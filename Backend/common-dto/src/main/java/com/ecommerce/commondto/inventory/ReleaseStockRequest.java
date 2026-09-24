package com.ecommerce.commondto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO to release (rollback) reserved stock when an order is cancelled.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStockRequest {

    @NotNull
    private UUID orderId;
}

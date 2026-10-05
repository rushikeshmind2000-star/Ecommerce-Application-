package com.ecommerce.commondto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO to confirm (deduct) reserved stock after a successful order payment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmStockRequest {

    @NotNull
    private UUID orderId;
}

package com.ecommerce.commondto.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Response DTO representing the current inventory state of a product.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private UUID productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
}

package com.ecommerce.inventoryservice.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
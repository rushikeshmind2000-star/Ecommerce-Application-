package com.ecommerce.inventoryservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {
    private UUID id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String currency;
    private String status;   // ACTIVE / INACTIVE
}
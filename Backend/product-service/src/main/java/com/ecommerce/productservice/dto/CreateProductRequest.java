package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductRequest {
    @NotNull(message = "Category ID is mandatory")
    private UUID categoryId;
    
    @NotBlank(message = "Product name is mandatory")
    private String name;
    
    private String description;
    
    @NotBlank(message = "SKU is mandatory")
    private String sku;
    
    @NotNull(message = "Price is mandatory")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private String currency = "INR";
    private String brand;
    
    private List<ProductImageRequest> images;
}

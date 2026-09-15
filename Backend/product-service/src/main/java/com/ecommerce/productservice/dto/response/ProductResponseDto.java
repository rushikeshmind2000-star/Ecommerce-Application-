package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {

    private UUID id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String currency;
    private ProductStatus status;
    private String brand;
    private CategoryResponseDto category;
    private List<ProductImageResponseDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

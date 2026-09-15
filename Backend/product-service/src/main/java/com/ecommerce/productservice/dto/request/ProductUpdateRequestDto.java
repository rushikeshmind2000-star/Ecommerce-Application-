package com.ecommerce.productservice.dto.request;

import com.ecommerce.productservice.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequestDto {

    @NotNull(message = "Product ID is required")
    private UUID id;

    private UUID categoryId;

    private CategoryRequestDto category;

    private String name;

    private String description;

    private String sku;

    private BigDecimal price;

    private String currency;

    private ProductStatus status;

    private String brand;

    private List<ProductImageRequestDto> images;
}

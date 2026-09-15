package com.ecommerce.productservice.dto.request;

import com.ecommerce.productservice.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class ProductRequestDto {

    private UUID categoryId;

    private CategoryRequestDto category;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Product status is required")
    private ProductStatus status;

    @NotBlank(message = "Brand is required")
    private String brand;

    @Valid
    private List<ProductImageRequestDto> images;
}

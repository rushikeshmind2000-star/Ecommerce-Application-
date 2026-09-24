package com.ecommerce.commondto.product;

import com.ecommerce.commondto.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO representing a full product with its images.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String currency;
    private ProductStatus status;
    private String brand;
    private List<ProductImageDTO> images;
}

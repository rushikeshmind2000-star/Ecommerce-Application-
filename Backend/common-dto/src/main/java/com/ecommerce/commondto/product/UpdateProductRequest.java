package com.ecommerce.commondto.product;

import com.ecommerce.commondto.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO to update an existing product.
 */
@Data
public class UpdateProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private String brand;
    private List<ProductImageRequest> images;
}

package com.ecommerce.productservice.dto;

import com.ecommerce.productservice.entity.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private String brand;
    private List<ProductImageRequest> images;
}

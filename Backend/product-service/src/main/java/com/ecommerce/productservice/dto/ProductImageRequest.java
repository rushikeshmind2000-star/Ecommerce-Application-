package com.ecommerce.productservice.dto;

import lombok.Data;

@Data
public class ProductImageRequest {
    private String imageUrl;
    private boolean isPrimary;
}

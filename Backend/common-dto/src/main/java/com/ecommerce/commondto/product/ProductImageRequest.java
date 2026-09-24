package com.ecommerce.commondto.product;

import lombok.Data;

/**
 * Request DTO for a product image (used during product create/update).
 */
@Data
public class ProductImageRequest {

    private String imageUrl;
    private boolean isPrimary;
}

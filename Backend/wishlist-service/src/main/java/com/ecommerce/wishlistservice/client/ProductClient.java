package com.ecommerce.wishlistservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feign client for communicating with product-service.
 * Used to validate product existence, status, and fetch product details.
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{productId}")
    ProductResponse getProduct(@PathVariable("productId") UUID productId);

    record ProductResponse(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        String status,
        String imageUrl
    ) {}
}

package com.ecommerce.inventoryservice.client;

import com.ecommerce.inventoryservice.client.dto.ProductResponse;
import com.ecommerce.inventoryservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "product-service",
        path = "/api/products",
        configuration = FeignConfig.class)
public interface ProductClient {

    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable("id") UUID id);
}
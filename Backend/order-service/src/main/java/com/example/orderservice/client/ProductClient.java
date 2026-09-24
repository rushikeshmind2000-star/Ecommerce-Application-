package com.example.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for the Product Service.
 * Protected with a Circuit Breaker and Retry to handle downstream failures gracefully.
 * Fallback returns {@code null} so the caller can decide whether to proceed.
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    Logger log = LoggerFactory.getLogger(ProductClient.class);

    /**
     * Fetches a product by ID.
     * Falls back to {@link #getProductFallback} when the circuit is OPEN or the call fails.
     */
    @GetMapping("/api/products/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService")
    Object getProduct(@PathVariable("id") UUID id);

    /**
     * Fallback: invoked when product-service is unavailable or circuit is OPEN.
     * Returns {@code null} so the order flow can continue with cached/default data.
     */
    default Object getProductFallback(UUID id, Exception ex) {
        log.warn("[Circuit Breaker] product-service unavailable for productId={}. Reason: {}",
                id, ex.getMessage());
        return null;
    }
}

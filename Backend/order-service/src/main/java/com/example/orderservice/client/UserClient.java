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
 * Feign client for the User Service.
 * Protected with a Circuit Breaker and Retry to handle downstream failures gracefully.
 */
@FeignClient(name = "user-service")
public interface UserClient {

    Logger log = LoggerFactory.getLogger(UserClient.class);

    /**
     * Fetches a user by ID.
     * Falls back to {@link #getUserFallback} when circuit is OPEN or call fails.
     */
    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    Object getUser(@PathVariable("id") UUID id);

    /**
     * Fallback: invoked when user-service is unavailable or circuit is OPEN.
     * Returns {@code null} so the caller can apply its own degradation strategy.
     */
    default Object getUserFallback(UUID id, Exception ex) {
        log.warn("[Circuit Breaker] user-service unavailable for userId={}. Reason: {}",
                id, ex.getMessage());
        return null;
    }
}

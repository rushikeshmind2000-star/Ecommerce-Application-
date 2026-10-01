package com.ecommerce.wishlistservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for communicating with user-service.
 * Used to validate that the user exists before adding to wishlist.
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserResponse getUser(@PathVariable("userId") UUID userId);

    record UserResponse(UUID id, String username, String email) {}
}

package com.ecommerce.wishlistservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(name = "cart-service")
public interface CartClient {

    @PostMapping("/api/cart")
    Object addToCart(@RequestBody CartRequest request);

    record CartRequest(UUID userId, UUID productId, Integer quantity) {}
}

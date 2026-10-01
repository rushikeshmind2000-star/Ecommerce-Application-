package com.ecommerce.wishlistservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/api/orders")
    Object createOrder(@RequestBody CreateOrderRequest request);

    record CreateOrderRequest(
            UUID userId,
            String shippingAddress,
            String billingAddress,
            String currency,
            List<OrderItemRequest> items
    ) {}

    record OrderItemRequest(
            UUID productId,
            String productName,
            String sku,
            Integer quantity,
            BigDecimal unitPrice
    ) {}
}

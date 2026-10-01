package com.ecommerce.cartservice.client;

import com.ecommerce.cartservice.dto.CreateOrderRequest;
import com.ecommerce.cartservice.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderClient {

    @PostMapping("/api/orders")
    OrderDTO createOrder(@RequestBody CreateOrderRequest request);
}

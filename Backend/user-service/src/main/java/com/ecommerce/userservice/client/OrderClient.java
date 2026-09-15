package com.ecommerce.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    Object getOrder(@PathVariable("id") UUID id);
}

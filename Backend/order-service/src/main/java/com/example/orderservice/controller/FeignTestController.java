package com.example.orderservice.controller;

import com.example.orderservice.client.PaymentClient;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders/test-feign")
public class FeignTestController {

    @Autowired
    private UserClient userClient;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private PaymentClient paymentClient;

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> testUserClient(@PathVariable UUID id) {
        Object response = userClient.getUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> testProductClient(@PathVariable UUID id) {
        Object response = productClient.getProduct(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payments")
    public ResponseEntity<Object> testPaymentClient(@RequestBody Object paymentRequest) {
        Object response = paymentClient.processPayment(paymentRequest);
        return ResponseEntity.ok(response);
    }
}

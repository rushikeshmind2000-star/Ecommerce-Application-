package com.example.orderservice.client;

import com.example.orderservice.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the Payment Service.
 * Protected with a Circuit Breaker that hard-fails when the circuit is OPEN,
 * because payment is a critical operation — silent degradation is not safe.
 */
@FeignClient(name = "payment-service")
public interface PaymentClient {

    Logger log = LoggerFactory.getLogger(PaymentClient.class);

    /**
     * Processes a payment request.
     * Falls back to {@link #processPaymentFallback} when circuit is OPEN or call fails.
     */
    @PostMapping("/api/payments")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    Object processPayment(@RequestBody Object paymentRequest);

    /**
     * Fallback: throws {@link ServiceUnavailableException} (HTTP 503).
     * Payment must not be silently skipped — the client is informed to retry later.
     */
    default Object processPaymentFallback(Object paymentRequest, Exception ex) {
        log.error("[Circuit Breaker] payment-service unavailable. Reason: {}", ex.getMessage());
        throw new ServiceUnavailableException(
                "Payment service is currently unavailable. Please try again in a few moments."
        );
    }
}

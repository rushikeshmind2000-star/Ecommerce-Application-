package com.ecommerce.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for the API Gateway circuit breaker.
 *
 * <p>When a downstream service's circuit breaker is OPEN (or the service returns
 * a non-retryable error), Spring Cloud Gateway forwards the request to
 * {@code /fallback/{service}} instead of propagating the error to the client.
 *
 * <p>This controller returns a structured JSON 503 response so clients always
 * receive a consistent, human-readable error rather than a raw timeout.
 */
@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    /**
     * Fallback for GET requests (product lookups, order queries, user profile, etc.)
     *
     * @param service the service name segment from the route (e.g. "order-service")
     * @return 503 Service Unavailable with structured error body
     */
    @GetMapping("/{service}")
    public Mono<ResponseEntity<Map<String, Object>>> getFallback(
            @PathVariable String service) {

        return Mono.just(buildResponse(service));
    }

    /**
     * Fallback for POST requests (create order, process payment, etc.)
     *
     * @param service the service name segment from the route
     * @return 503 Service Unavailable with structured error body
     */
    @PostMapping("/{service}")
    public Mono<ResponseEntity<Map<String, Object>>> postFallback(
            @PathVariable String service) {

        return Mono.just(buildResponse(service));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildResponse(String service) {
        Map<String, Object> body = Map.of(
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "service", service,
                "message", String.format(
                        "The '%s' is temporarily unavailable. " +
                        "The circuit breaker is OPEN due to repeated failures. " +
                        "Please try again in a few moments.",
                        service
                ),
                "timestamp", Instant.now().toString()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(body);
    }
}

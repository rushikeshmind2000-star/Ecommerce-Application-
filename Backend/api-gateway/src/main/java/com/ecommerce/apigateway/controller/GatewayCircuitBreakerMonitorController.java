package com.ecommerce.apigateway.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gateway-level Circuit Breaker Monitor.
 *
 * <p>
 * Exposes real-time state, metrics, and health of every circuit breaker
 * registered in the API Gateway (one per downstream service route).
 *
 * <p>
 * Endpoints:
 * <ul>
 * <li>{@code GET /monitor/circuit-breakers} — all CBs summary</li>
 * <li>{@code GET /monitor/circuit-breakers/{name}} — single CB detail</li>
 * <li>{@code GET /monitor/circuit-breakers/health} — simple HEALTHY / DEGRADED
 * check</li>
 * </ul>
 */
@RestController
@RequestMapping("/monitor/circuit-breakers")
public class GatewayCircuitBreakerMonitorController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public GatewayCircuitBreakerMonitorController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    // ── All CBs ────────────────────────────────────────────────────────────────
    /**
     * Returns a list of all gateway circuit breakers with live metrics.
     * <p>
     * Example: {@code GET http://localhost:8085/monitor/circuit-breakers}
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getAllCircuitBreakers() {

        List<Map<String, Object>> breakers = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .map(this::buildSummary)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("gateway", "api-gateway");
        response.put("totalCircuitBreakers", breakers.size());
        response.put("circuitBreakers", breakers);

        return Mono.just(ResponseEntity.ok(response));
    }

    // ── Single CB ──────────────────────────────────────────────────────────────
    /**
     * Returns full detail for a single gateway circuit breaker.
     * <p>
     * Example:
     * {@code GET http://localhost:8085/monitor/circuit-breakers/orderServiceCB}
     *
     * @param name the circuit breaker name (e.g. orderServiceCB,
     * paymentServiceCB)
     */
    @GetMapping("/{name}")
    public Mono<ResponseEntity<Map<String, Object>>> getCircuitBreakerByName(
            @PathVariable String name) {

        return circuitBreakerRegistry.find(name)
                .map(cb -> Mono.just(ResponseEntity.ok(buildDetailedReport(cb))))
                .orElse(Mono.just(ResponseEntity.notFound().build()));
    }

    // ── Health Check ───────────────────────────────────────────────────────────
    /**
     * Quick health snapshot: {@code 200} if all CBs are CLOSED, {@code 503}
     * otherwise.
     * <p>
     * Example:
     * {@code GET http://localhost:8085/monitor/circuit-breakers/health}
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getHealth() {

        List<Map<String, Object>> statuses = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .map(cb -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("name", cb.getName());
                    s.put("state", cb.getState().name());
                    s.put("healthy", cb.getState() == CircuitBreaker.State.CLOSED);
                    s.put("failureRate", formatRate(cb.getMetrics().getFailureRate()) + "%");
                    s.put("notPermittedCalls", cb.getMetrics().getNumberOfNotPermittedCalls());
                    return s;
                })
                .collect(Collectors.toList());

        boolean allHealthy = statuses.stream()
                .allMatch(s -> Boolean.TRUE.equals(s.get("healthy")));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("gateway", "api-gateway");
        response.put("overall", allHealthy ? "HEALTHY" : "DEGRADED");
        response.put("services", statuses);

        HttpStatus status = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return Mono.just(ResponseEntity.status(status).body(response));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private Map<String, Object> buildSummary(CircuitBreaker cb) {
        CircuitBreaker.Metrics m = cb.getMetrics();

        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", cb.getName());
        s.put("state", cb.getState().name());
        s.put("failureRate", formatRate(m.getFailureRate()) + "%");
        s.put("slowCallRate", formatRate(m.getSlowCallRate()) + "%");
        s.put("bufferedCalls", m.getNumberOfBufferedCalls());
        s.put("failedCalls", m.getNumberOfFailedCalls());
        s.put("successfulCalls", m.getNumberOfSuccessfulCalls());
        s.put("notPermittedCalls", m.getNumberOfNotPermittedCalls());
        return s;
    }

    private Map<String, Object> buildDetailedReport(CircuitBreaker cb) {
        CircuitBreaker.Metrics m = cb.getMetrics();

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("slidingWindowSize", cb.getCircuitBreakerConfig().getSlidingWindowSize());
        config.put("failureRateThreshold", cb.getCircuitBreakerConfig().getFailureRateThreshold() + "%");
        config.put("waitDurationInOpenState", cb.getCircuitBreakerConfig().getWaitIntervalFunctionInOpenState().apply(1).toString() + "ns");
        config.put("permittedCallsInHalfOpenState", cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState());
        config.put("minimumNumberOfCalls", cb.getCircuitBreakerConfig().getMinimumNumberOfCalls());
        config.put("automaticTransition", cb.getCircuitBreakerConfig().isAutomaticTransitionFromOpenToHalfOpenEnabled());

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("failureRate", formatRate(m.getFailureRate()) + "%");
        metrics.put("slowCallRate", formatRate(m.getSlowCallRate()) + "%");
        metrics.put("bufferedCalls", m.getNumberOfBufferedCalls());
        metrics.put("failedCalls", m.getNumberOfFailedCalls());
        metrics.put("successfulCalls", m.getNumberOfSuccessfulCalls());
        metrics.put("slowCalls", m.getNumberOfSlowCalls());
        metrics.put("notPermittedCalls", m.getNumberOfNotPermittedCalls());

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("timestamp", Instant.now().toString());
        detail.put("name", cb.getName());
        detail.put("state", cb.getState().name());
        detail.put("config", config);
        detail.put("metrics", metrics);
        return detail;
    }

    private String formatRate(float rate) {
        return rate < 0 ? "N/A" : String.format("%.1f", rate);
    }
}

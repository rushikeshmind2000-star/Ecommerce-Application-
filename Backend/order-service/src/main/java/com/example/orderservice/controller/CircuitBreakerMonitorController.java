package com.example.orderservice.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Circuit Breaker Monitor Controller.
 *
 * <p>Provides a developer-friendly REST API to inspect the real-time state
 * of every Resilience4j circuit breaker registered in this service.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api/monitor/circuit-breakers}          — all CBs summary</li>
 *   <li>{@code GET /api/monitor/circuit-breakers/{name}}   — single CB detail</li>
 *   <li>{@code GET /api/monitor/circuit-breakers/health}   — quick health check (CLOSED = healthy)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/circuit-breakers")
@RequiredArgsConstructor
public class CircuitBreakerMonitorController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    // ── All CBs Summary ────────────────────────────────────────────────────────

    /**
     * Returns a summary of ALL registered circuit breakers with their current
     * state, failure rate, call counts, and slow-call metrics.
     *
     * <p>Example: {@code GET /api/monitor/circuit-breakers}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCircuitBreakers() {

        List<Map<String, Object>> breakers = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .map(this::buildSummary)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("totalCircuitBreakers", breakers.size());
        response.put("circuitBreakers", breakers);

        log.debug("Circuit breaker monitor queried — {} breakers found", breakers.size());
        return ResponseEntity.ok(response);
    }

    // ── Single CB Detail ───────────────────────────────────────────────────────

    /**
     * Returns detailed metrics for a single circuit breaker by name.
     *
     * <p>Example: {@code GET /api/monitor/circuit-breakers/productService}
     *
     * @param name the circuit breaker name (e.g. productService, paymentService, userService)
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerByName(
            @PathVariable String name) {

        return circuitBreakerRegistry.find(name)
                .map(cb -> ResponseEntity.ok(buildDetailedReport(cb)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Quick Health Check ─────────────────────────────────────────────────────

    /**
     * Returns a simplified health snapshot of all circuit breakers.
     * Useful for CI/CD pipelines, load balancer health checks, and dashboards.
     *
     * <p>Example: {@code GET /api/monitor/circuit-breakers/health}
     *
     * <p>Response HTTP status:
     * <ul>
     *   <li>{@code 200 OK}            — all circuit breakers are CLOSED (healthy)</li>
     *   <li>{@code 503 Service Unavailable} — at least one CB is OPEN or HALF_OPEN</li>
     * </ul>
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {

        List<Map<String, Object>> statuses = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .map(cb -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("name", cb.getName());
                    s.put("state", cb.getState().name());
                    s.put("healthy", cb.getState() == CircuitBreaker.State.CLOSED);
                    return s;
                })
                .collect(Collectors.toList());

        boolean allHealthy = statuses.stream()
                .allMatch(s -> Boolean.TRUE.equals(s.get("healthy")));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("overall", allHealthy ? "HEALTHY" : "DEGRADED");
        response.put("services", statuses);

        return allHealthy
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(503).body(response);
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /**
     * Builds a compact summary map for list views.
     */
    private Map<String, Object> buildSummary(CircuitBreaker cb) {
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("name",            cb.getName());
        summary.put("state",           cb.getState().name());
        summary.put("failureRate",     formatRate(metrics.getFailureRate()) + "%");
        summary.put("slowCallRate",    formatRate(metrics.getSlowCallRate()) + "%");
        summary.put("bufferedCalls",   metrics.getNumberOfBufferedCalls());
        summary.put("failedCalls",     metrics.getNumberOfFailedCalls());
        summary.put("successfulCalls", metrics.getNumberOfSuccessfulCalls());
        summary.put("notPermittedCalls", metrics.getNumberOfNotPermittedCalls());
        return summary;
    }

    /**
     * Builds a detailed report map for the single-CB endpoint.
     */
    private Map<String, Object> buildDetailedReport(CircuitBreaker cb) {
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        // ── State & Config ────────────────────────────────────────────────────
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("slidingWindowSize",                cb.getCircuitBreakerConfig().getSlidingWindowSize());
        config.put("failureRateThreshold",             cb.getCircuitBreakerConfig().getFailureRateThreshold() + "%");
        config.put("slowCallRateThreshold",            cb.getCircuitBreakerConfig().getSlowCallRateThreshold() + "%");
        config.put("slowCallDurationThreshold",        cb.getCircuitBreakerConfig().getSlowCallDurationThreshold().toString());
        config.put("waitDurationInOpenState",          cb.getCircuitBreakerConfig().getWaitIntervalFunctionInOpenState().apply(1).toString() + "ns");

        config.put("permittedCallsInHalfOpenState",    cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState());
        config.put("minimumNumberOfCalls",             cb.getCircuitBreakerConfig().getMinimumNumberOfCalls());
        config.put("automaticTransitionEnabled",       cb.getCircuitBreakerConfig().isAutomaticTransitionFromOpenToHalfOpenEnabled());

        // ── Live Metrics ──────────────────────────────────────────────────────
        Map<String, Object> liveMetrics = new LinkedHashMap<>();
        liveMetrics.put("failureRate",          formatRate(metrics.getFailureRate()) + "%");
        liveMetrics.put("slowCallRate",         formatRate(metrics.getSlowCallRate()) + "%");
        liveMetrics.put("bufferedCalls",        metrics.getNumberOfBufferedCalls());
        liveMetrics.put("failedCalls",          metrics.getNumberOfFailedCalls());
        liveMetrics.put("successfulCalls",      metrics.getNumberOfSuccessfulCalls());
        liveMetrics.put("slowCalls",            metrics.getNumberOfSlowCalls());
        liveMetrics.put("slowSuccessfulCalls",  metrics.getNumberOfSlowSuccessfulCalls());
        liveMetrics.put("slowFailedCalls",      metrics.getNumberOfSlowFailedCalls());
        liveMetrics.put("notPermittedCalls",    metrics.getNumberOfNotPermittedCalls());

        // ── Full Response ─────────────────────────────────────────────────────
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("timestamp",  Instant.now().toString());
        detail.put("name",       cb.getName());
        detail.put("state",      cb.getState().name());
        detail.put("config",     config);
        detail.put("metrics",    liveMetrics);

        return detail;
    }

    /**
     * Formats a float rate as a readable string; returns "N/A" when no calls
     * have been recorded yet (Resilience4j returns -1.0f in that case).
     */
    private String formatRate(float rate) {
        return rate < 0 ? "N/A" : String.format("%.1f", rate);
    }
}

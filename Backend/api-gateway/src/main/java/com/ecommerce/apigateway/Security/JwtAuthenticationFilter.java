package com.ecommerce.apigateway.Security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global JWT Authentication Filter for API Gateway.
 *
 * Flow:
 *   1. Check if route is public (whitelist) → skip validation
 *   2. Extract "Authorization: Bearer <token>" header
 *   3. Validate JWT signature + expiry
 *   4. Forward userId, email, role as headers to downstream services
 *
 * Downstream services can read X-User-Id, X-User-Email, X-User-Role headers
 * without needing to validate JWT themselves.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;

    /** Routes that do NOT require a JWT */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/users/register",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/fallback"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ── 1. Skip public routes & OPTIONS ───────────────────────────────────
        if (isPublicPath(path) || request.getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        // ── 2. Extract Authorization header ─────────────────────────────────
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // ── 3. Validate token ────────────────────────────────────────────────
        if (!jwtValidator.isValid(token)) {
            log.warn("Invalid or expired JWT for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // ── 4. Extract claims and forward as headers to downstream ───────────
        try {
            Claims claims = jwtValidator.validateAndExtract(token);
            String userId = claims.get("userId", String.class);
            String email  = claims.getSubject();
            String role   = claims.get("role", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id",    userId)
                    .header("X-User-Email", email)
                    .header("X-User-Role",  role)
                    .build();

            log.debug("JWT valid — userId={}, role={}, path={}", userId, role, path);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("JWT processing error: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Run before all other filters
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}

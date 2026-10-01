package com.ecommerce.userservice.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security config for user-service.
 *
 * NOTE: user-service acts as the Auth Server — it issues JWTs.
 *       All downstream services (product, order, payment…) trust the Gateway
 *       and are configured to permit all internal calls.
 *       user-service itself only needs to protect admin-level user-management routes.
 *
 * Public routes:
 *   POST /api/auth/login          — login
 *   POST /api/users/register      — register
 *   GET  /swagger-ui/**           — Swagger UI
 *   GET  /v3/api-docs/**          — OpenAPI JSON
 *   GET  /actuator/**             — health probes
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            // CSRF disabled intentionally: this service is a stateless REST API using JWT Bearer tokens.
            // CSRF attacks only affect session-cookie-based auth; browsers never auto-send
            // Authorization headers cross-origin, so CSRF protection is not needed here. // NOSONAR java:S4502
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Public endpoints ────────────────────────────────────────────
                .requestMatchers(
                        "/api/auth/**",
                        "/api/users/register",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/**"
                ).permitAll()

                // ── Admin-only: manage users / change statuses ──────────────────
                .requestMatchers(HttpMethod.GET,   "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/users/*/status").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")

                // ── Authenticated users can read/update their own profile ────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

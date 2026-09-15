package com.ecommerce.apigateway.config;

import com.ecommerce.apigateway.Security.KeycloakRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GatewayConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                new KeycloakRoleConverter()
        );

        ReactiveJwtAuthenticationConverterAdapter reactiveConverter =
                new ReactiveJwtAuthenticationConverterAdapter(converter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        // Public APIs
                        .pathMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/users/register",
                                "/actuator/health"
                        ).permitAll()

                        // ADMIN only
                        .pathMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // USER or ADMIN
                        .pathMatchers("/api/users/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Everything else requires authentication
                        .anyExchange()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        reactiveConverter
                                )
                        )
                )

                .build();
    }
}
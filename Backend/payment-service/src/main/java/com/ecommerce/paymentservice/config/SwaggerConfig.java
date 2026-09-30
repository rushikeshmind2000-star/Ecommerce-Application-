package com.ecommerce.paymentservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "💳 Payment Service API",
                version = "1.0",
                description = """
                        **Payment processing for the Ecommerce platform.**
                        
                        ### Payment States
                        | Status | Meaning |
                        |--------|---------|
                        | `PENDING` | Payment initiated, awaiting gateway response |
                        | `SUCCESS` | Payment completed successfully |
                        | `FAILED` | Payment declined or timed out |
                        | `REFUNDED` | Payment reversed after successful refund |
                        
                        ### ⚠️ Important
                        - **Do NOT retry** `POST /api/payments` on failure — it may cause **double charges**
                        - Refunds are Admin-only and can only be processed on SUCCESS payments
                        
                        > 🔒 Authenticate via **user-service** `POST /api/auth/login` then click **Authorize 🔒**
                        """,
                contact = @Contact(name = "Ecommerce Team", email = "birajdarrohit56@gmail.com")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Paste: Bearer <access_token>  (get token from user-service /api/auth/login)",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8085").description("Via API Gateway"),
                        new Server().url("http://localhost:8086").description("Direct — Payment Service")
                ));
    }
}

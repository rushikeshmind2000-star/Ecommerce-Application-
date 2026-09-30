package com.ecommerce.inventoryservice.config;

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
                title = "📦 Inventory Service API",
                version = "1.0",
                description = """
                        **Stock level management for the Ecommerce platform.**
                        
                        ### Stock Lifecycle (Saga Pattern)
                        | Step | Endpoint | Description |
                        |------|----------|-------------|
                        | 1. Create stock | `POST /api/inventory` | Admin/Vendor initialises stock after product approved |
                        | 2. Reserve | `POST /api/inventory/reserve` | Holds stock when order is placed |
                        | 3a. Confirm | `POST /api/inventory/confirm` | Deducts stock after successful payment |
                        | 3b. Release | `POST /api/inventory/release` | Returns stock if payment fails / order cancelled |
                        | 4. Check stock | `GET /api/inventory/{productId}` | View current available vs reserved |
                        
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
                        new Server().url("http://localhost:8084").description("Direct — Inventory Service")
                ));
    }
}

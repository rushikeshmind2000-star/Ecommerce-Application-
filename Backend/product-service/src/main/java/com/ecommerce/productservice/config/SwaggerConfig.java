package com.ecommerce.productservice.config;

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

/**
 * Swagger / OpenAPI config for product-service.
 *
 * ▶ Adds a Bearer JWT Authorize button.
 * ▶ Authentication is enforced by the API Gateway — this service
 *   reads X-User-Id, X-User-Email, X-User-Role headers injected by the Gateway.
 *
 * Roles:
 *   VENDOR  → submit products, view own products
 *   ADMIN   → approve / reject products, view all
 *   CUSTOMER→ browse active products, read by SKU / category
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "🛍️ Product Service API",
                version = "1.0",
                description = """
                        **Product catalog & approval workflow for the Ecommerce platform.**
                        
                        ### Approval Workflow
                        | Step | Who | Endpoint |
                        |------|-----|----------|
                        | 1. Submit product | Vendor | `POST /api/products` |
                        | 2. List pending | Admin | `GET /api/products?status=PENDING` |
                        | 3. Approve | Admin | `PATCH /api/products/{id}/approve` |
                        | 4. Reject + reason | Admin | `PATCH /api/products/{id}/reject` |
                        | 5. Check status | Vendor | `GET /api/products/vendor/{vendorId}` |
                        | 6. Browse store | Customer | `GET /api/products/active` |
                        
                        ### How to Authenticate
                        1. Login via **user-service** → `POST /api/auth/login`
                        2. Copy the `accessToken`
                        3. Click the **Authorize 🔒** button above → paste `Bearer <token>`
                        """,
                contact = @Contact(name = "Rushikesh Mind", email = "rushikeshmind@gmail.com")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Paste your JWT access token here. Get one from POST /api/auth/login on user-service.",
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
                        new Server().url("http://localhost:8082").description("Direct — Product Service"),
                        new Server().url("http://localhost:8085").description("Via API Gateway")
                ));
    }
}

package com.ecommerce.userservice.Security;

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
 * Swagger / OpenAPI config for user-service.
 * This service is the JWT issuer — login here first, then use the token everywhere else.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "🔐 User Service API",
                version = "1.0",
                description = """
                        **Identity & User Management for the Ecommerce platform.**
                        
                        ### Quick Start
                        1. **Register** → `POST /api/users/register` (choose role: CUSTOMER / VENDOR)
                        2. **Login** → `POST /api/auth/login` — copy the `accessToken`
                        3. **Authorize** → Click 🔒 Authorize above → paste `Bearer <token>`
                        4. Now all protected endpoints will include your token automatically
                        
                        ### Roles & Access
                        | Role | Status on Register | Can Login Immediately? |
                        |------|--------------------|------------------------|
                        | CUSTOMER | ACTIVE | ✅ Yes |
                        | VENDOR | **PENDING** | ❌ Wait for Admin approval |
                        | ADMIN | ACTIVE | ✅ Yes |
                        
                        > Admin must call `PATCH /api/users/{id}/status?status=ACTIVE` to approve a VENDOR.
                        """,
                contact = @Contact(name = "Rushikesh Mind", email = "rushikeshmind@gmail.com")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Login via POST /api/auth/login to get your JWT token, then paste it here as: Bearer <token>",
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
                        new Server().url("http://localhost:8081").description("Direct — User Service"),
                        new Server().url("http://localhost:8085").description("Via API Gateway")
                ));
    }
}
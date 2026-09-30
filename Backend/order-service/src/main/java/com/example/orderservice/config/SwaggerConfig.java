package com.example.orderservice.config;

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
                title = "📋 Order Service API",
                version = "1.0",
                description = """
                        **Order management for the Ecommerce platform.**
                        
                        ### Order Status Flow
                        ```
                        PENDING → CONFIRMED → SHIPPED → DELIVERED
                              ↘ CANCELLED (before shipping)
                        ```
                        
                        ### Order Saga (automatic, internal)
                        When `POST /api/orders` is called, the system automatically:
                        1. Reserves stock in Inventory Service
                        2. Initiates payment in Payment Service
                        3. On payment success → confirms stock + sets order CONFIRMED
                        4. On payment failure → releases stock + sets order CANCELLED
                        
                        ### Access Rules
                        | Endpoint | CUSTOMER | ADMIN |
                        |----------|----------|-------|
                        | Place order | ✅ (own) | ✅ |
                        | View order | ✅ (own) | ✅ |
                        | View all orders | ❌ | ✅ |
                        | Cancel order | ✅ (own, if PENDING) | ✅ |
                        
                        > 🔒 Authenticate via **user-service** `POST /api/auth/login` then click **Authorize 🔒**
                        """,
                contact = @Contact(name = "Rushikesh Mind", email = "rushikeshmind@gmail.com")
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
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8085").description("Via API Gateway"),
                        new Server().url("http://localhost:8083").description("Direct — Order Service")
                ));
    }
}

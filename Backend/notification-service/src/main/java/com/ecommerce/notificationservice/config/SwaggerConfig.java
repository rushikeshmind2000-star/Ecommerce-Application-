package com.ecommerce.notificationservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI config for notification-service.
 *
 * Adds Bearer JWT Authorize button so you can test protected
 * endpoints directly from Swagger UI.
 *
 * NOTE: Authentication is enforced by the API Gateway.
 *       The Gateway injects X-User-Id, X-User-Email, X-User-Role
 *       headers which this service can read from the request.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ecommerce Notification Service API",
                version = "1.0",
                description = "Notification management — email, SMS, push alerts"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI();
    }
}

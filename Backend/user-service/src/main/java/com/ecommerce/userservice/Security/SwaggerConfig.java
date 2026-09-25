package com.ecommerce.userservice.Security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ecommerce User Service API",
                version = "1.0",
                description = "User Registration and User APIs",
                contact = @Contact(
                        name = "Rohit Birajdar",
                        email = "birajdarrohit56@gmail.com"
                )
        )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI();
    }
}
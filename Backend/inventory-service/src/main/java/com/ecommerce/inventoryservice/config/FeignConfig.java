package com.ecommerce.inventoryservice.config;

import com.ecommerce.inventoryservice.exceptions.ProductNotFoundException;
import com.ecommerce.inventoryservice.exceptions.ProductServiceUnavailableException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        ErrorDecoder defaultDecoder = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new ProductNotFoundException("Product not found");
            }
            if (response.status() >= 500) {
                return new ProductServiceUnavailableException(
                        "Product service returned HTTP " + response.status());
            }
            return defaultDecoder.decode(methodKey, response);
        };
    }
}
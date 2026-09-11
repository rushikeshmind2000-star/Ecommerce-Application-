package com.ecommerce.userservice.Exception;

public class JwtAuthenticationEntryPoint extends RuntimeException {
    public JwtAuthenticationEntryPoint(String message) {
        super(message);
    }
}

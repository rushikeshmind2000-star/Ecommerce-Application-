package com.ecommerce.userservice.Exception;

public class AuthenticationEntryPoint extends RuntimeException {
    public AuthenticationEntryPoint(String message) {
        super(message);
    }
}

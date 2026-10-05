package com.ecommerce.inventoryservice.exceptions;

public class InvalidProductResponseException extends RuntimeException {

    public InvalidProductResponseException(String message) {
        super(message);
    }
}

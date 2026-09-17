package com.ecommerce.inventoryservice.exceptions;

public class ProductServiceUnavailableException extends RuntimeException {
    public ProductServiceUnavailableException(String m) { super(m); }
}
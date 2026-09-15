package com.example.orderservice.exception;

/**
 * Thrown when an order cancellation is rejected because the order
 * is already in a terminal state (DELIVERED or CANCELLED).
 */
public class OrderCancellationException extends RuntimeException {

    public OrderCancellationException(String message) {
        super(message);
    }
}

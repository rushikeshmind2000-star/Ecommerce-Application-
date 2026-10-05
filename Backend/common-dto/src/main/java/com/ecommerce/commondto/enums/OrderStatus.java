package com.ecommerce.commondto.enums;

/**
 * Lifecycle states for an Order.
 *
 * Allowed transitions:
 *   PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *   Any state (except DELIVERED / CANCELLED) → CANCELLED
 */
public enum OrderStatus {

    /** Order placed but not yet confirmed (e.g. payment/inventory pending). */
    PENDING,

    /** Saga in progress. */
    PROCESSING_SAGA,

    /** Payment received; order confirmed. */
    CONFIRMED,

    /** Order is being packed / prepared. */
    PROCESSING,

    /** Order dispatched to courier. */
    SHIPPED,

    /** Order delivered to customer. */
    DELIVERED,

    /** Order cancelled by customer or system. */
    CANCELLED
}

package com.ecommerce.wishlistservice.exception;

/** Thrown when a wishlist item is not found */
public class WishlistItemNotFoundException extends RuntimeException {
    public WishlistItemNotFoundException(String message) {
        super(message);
    }
}

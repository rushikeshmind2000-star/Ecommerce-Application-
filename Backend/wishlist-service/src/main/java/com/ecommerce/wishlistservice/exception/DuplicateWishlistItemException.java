package com.ecommerce.wishlistservice.exception;

/** Thrown when an item is already in the user's wishlist (duplicate) */
public class DuplicateWishlistItemException extends RuntimeException {
    public DuplicateWishlistItemException(String message) {
        super(message);
    }
}

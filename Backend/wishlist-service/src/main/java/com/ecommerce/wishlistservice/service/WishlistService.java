package com.ecommerce.wishlistservice.service;

import com.ecommerce.wishlistservice.dto.AddWishlistRequest;
import com.ecommerce.wishlistservice.dto.WishlistItemResponse;
import com.ecommerce.wishlistservice.dto.WishlistResponse;

import java.util.UUID;

public interface WishlistService {

    /**
     * Add a product to the user's wishlist.
     * Validates user and product via Feign, rejects duplicates.
     */
    WishlistItemResponse addToWishlist(AddWishlistRequest request);

    /**
     * Get the full wishlist for a user, enriched with product details.
     */
    WishlistResponse getWishlist(UUID userId);

    /**
     * Remove a specific product from the user's wishlist.
     */
    void removeFromWishlist(UUID userId, UUID productId);

    /**
     * Moves an item from wishlist to cart.
     */
    void moveToCart(UUID userId, UUID productId);

    /**
     * Places a direct order for a wishlist item.
     */
    Object directOrder(UUID userId, UUID productId, com.ecommerce.wishlistservice.dto.WishlistOrderRequest request);
}

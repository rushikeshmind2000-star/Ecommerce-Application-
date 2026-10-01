package com.ecommerce.wishlistservice.controller;

import com.ecommerce.wishlistservice.dto.AddWishlistRequest;
import com.ecommerce.wishlistservice.dto.WishlistItemResponse;
import com.ecommerce.wishlistservice.dto.WishlistResponse;
import com.ecommerce.wishlistservice.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for the Wishlist Service.
 *
 * Endpoints:
 *   POST   /api/wishlist                        → Add product to wishlist
 *   GET    /api/wishlist/{userId}               → Get user's wishlist
 *   DELETE /api/wishlist/{userId}/{productId}   → Remove product from wishlist
 */
@Tag(name = "❤️ Wishlist", description = "Manage user wishlists")
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // ── POST /api/wishlist ─────────────────────────────────────────────────────
    @Operation(summary = "Add to wishlist",
               description = "Adds a product to the user's wishlist. Validates user and product. Rejects duplicates.")
    @PostMapping
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            @Valid @RequestBody AddWishlistRequest request) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(wishlistService.addToWishlist(request));
    }

    // ── GET /api/wishlist/{userId} ─────────────────────────────────────────────
    @Operation(summary = "Get wishlist",
               description = "Returns all wishlist items for the specified user, enriched with product details.")
    @GetMapping("/{userId}")
    public ResponseEntity<WishlistResponse> getWishlist(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(wishlistService.getWishlist(userId));
    }

    // ── DELETE /api/wishlist/{userId}/{productId} ──────────────────────────────
    @Operation(summary = "Remove from wishlist",
               description = "Removes a specific product from the user's wishlist.")
    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable UUID userId,
            @PathVariable UUID productId) {

        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/wishlist/{userId}/items/{productId}/move-to-cart ──────────────
    @Operation(summary = "Move to Cart",
               description = "Adds the wishlist item to the user's cart and removes it from the wishlist.")
    @PostMapping("/{userId}/items/{productId}/move-to-cart")
    public ResponseEntity<Void> moveToCart(
            @PathVariable UUID userId,
            @PathVariable UUID productId) {
        
        wishlistService.moveToCart(userId, productId);
        return ResponseEntity.ok().build();
    }

    // ── POST /api/wishlist/{userId}/items/{productId}/order ────────────────────
    @Operation(summary = "Order directly from Wishlist",
               description = "Creates a direct order for a specific wishlist item and removes it from wishlist.")
    @PostMapping("/{userId}/items/{productId}/order")
    public ResponseEntity<Object> directOrder(
            @PathVariable UUID userId,
            @PathVariable UUID productId,
            @Valid @RequestBody com.ecommerce.wishlistservice.dto.WishlistOrderRequest request) {
        
        return ResponseEntity.ok(wishlistService.directOrder(userId, productId, request));
    }
}

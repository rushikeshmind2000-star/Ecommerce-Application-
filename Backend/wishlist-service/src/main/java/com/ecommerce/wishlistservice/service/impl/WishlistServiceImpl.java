package com.ecommerce.wishlistservice.service.impl;

import com.ecommerce.wishlistservice.client.CartClient;
import com.ecommerce.wishlistservice.client.OrderClient;
import com.ecommerce.wishlistservice.client.ProductClient;
import com.ecommerce.wishlistservice.client.UserClient;
import com.ecommerce.wishlistservice.dto.AddWishlistRequest;
import com.ecommerce.wishlistservice.dto.WishlistItemResponse;
import com.ecommerce.wishlistservice.dto.WishlistOrderRequest;
import com.ecommerce.wishlistservice.dto.WishlistResponse;
import com.ecommerce.wishlistservice.entity.WishlistItem;
import com.ecommerce.wishlistservice.exception.DuplicateWishlistItemException;
import com.ecommerce.wishlistservice.exception.WishlistItemNotFoundException;
import com.ecommerce.wishlistservice.repository.WishlistRepository;
import com.ecommerce.wishlistservice.service.WishlistService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final CartClient cartClient;
    private final OrderClient orderClient;

    // ── Add to Wishlist ────────────────────────────────────────────────────────
    @Override
    @Transactional
    public WishlistItemResponse addToWishlist(AddWishlistRequest request) {
        UUID userId    = request.getUserId();
        UUID productId = request.getProductId();

        log.info("Adding product {} to wishlist for user {}", productId, userId);

        // 1. Validate user exists
        try {
            userClient.getUser(userId);
        } catch (FeignException.NotFound ex) {
            throw new WishlistItemNotFoundException("User not found with id: " + userId);
        }

        // 2. Validate product exists and is ACTIVE
        ProductClient.ProductResponse product;
        try {
            product = productClient.getProduct(productId);
        } catch (FeignException.NotFound ex) {
            throw new WishlistItemNotFoundException("Product not found with id: " + productId);
        }

        if (product.status() != null && !"ACTIVE".equalsIgnoreCase(product.status())) {
            throw new IllegalStateException(
                "Product '" + product.name() + "' is not available (status: " + product.status() + ")");
        }

        // 3. Prevent duplicates
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateWishlistItemException(
                "Product '" + product.name() + "' is already in your wishlist.");
        }

        // 4. Save and return
        WishlistItem saved = wishlistRepository.save(
            WishlistItem.builder()
                .userId(userId)
                .productId(productId)
                .build()
        );

        log.info("Wishlist item {} saved for user {}", saved.getId(), userId);
        return toItemResponse(saved, product);
    }

    // ── Get Wishlist ───────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(UUID userId) {
        log.info("Fetching wishlist for user {}", userId);

        List<WishlistItem> items = wishlistRepository.findByUserId(userId);

        List<WishlistItemResponse> enriched = items.stream()
            .map(item -> {
                ProductClient.ProductResponse product = null;
                try {
                    product = productClient.getProduct(item.getProductId());
                } catch (FeignException ex) {
                    log.warn("Could not fetch product {} details: {}", item.getProductId(), ex.getMessage());
                }
                return toItemResponse(item, product);
            })
            .toList();

        return WishlistResponse.builder()
            .userId(userId)
            .totalItems(enriched.size())
            .items(enriched)
            .build();
    }

    // ── Remove from Wishlist ───────────────────────────────────────────────────
    @Override
    @Transactional
    public void removeFromWishlist(UUID userId, UUID productId) {
        log.info("Removing product {} from wishlist for user {}", productId, userId);

        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new WishlistItemNotFoundException(
                "Product " + productId + " is not in the wishlist for user " + userId);
        }

        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Product {} removed from wishlist for user {}", productId, userId);
    }

    // ── Move to Cart ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void moveToCart(UUID userId, UUID productId) {
        log.info("Moving product {} from wishlist to cart for user {}", productId, userId);

        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new WishlistItemNotFoundException(
                "Product " + productId + " is not in the wishlist for user " + userId);
        }

        // Add to cart
        CartClient.CartRequest cartRequest = new CartClient.CartRequest(userId, productId, 1);
        cartClient.addToCart(cartRequest);

        // Remove from wishlist
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    // ── Direct Order ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Object directOrder(UUID userId, UUID productId, WishlistOrderRequest request) {
        log.info("Directly ordering product {} from wishlist for user {}", productId, userId);

        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new WishlistItemNotFoundException(
                "Product " + productId + " is not in the wishlist for user " + userId);
        }

        // 1. Fetch product details
        ProductClient.ProductResponse product;
        try {
            product = productClient.getProduct(productId);
        } catch (FeignException.NotFound ex) {
            throw new WishlistItemNotFoundException("Product not found with id: " + productId);
        }

        if (product.status() != null && !"ACTIVE".equalsIgnoreCase(product.status())) {
            throw new IllegalStateException("Product '" + product.name() + "' is not available to order.");
        }

        // 2. Build OrderItem
        OrderClient.OrderItemRequest orderItem = new OrderClient.OrderItemRequest(
            product.id(),
            product.name(),
            product.sku() != null ? product.sku() : "UNKNOWN-SKU",
            1, // quantity
            product.price()
        );

        // 3. Build CreateOrderRequest
        OrderClient.CreateOrderRequest createOrderReq = new OrderClient.CreateOrderRequest(
            userId,
            request.getShippingAddress(),
            request.getBillingAddress(),
            request.getCurrency() != null ? request.getCurrency() : "INR",
            List.of(orderItem)
        );

        // 4. Call order-service
        Object orderResponse = orderClient.createOrder(createOrderReq);

        // 5. Remove from wishlist since it's ordered
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);

        return orderResponse;
    }

    // ── Mapping Helpers ────────────────────────────────────────────────────────
    private WishlistItemResponse toItemResponse(WishlistItem item, ProductClient.ProductResponse product) {
        return WishlistItemResponse.builder()
            .id(item.getId())
            .userId(item.getUserId())
            .productId(item.getProductId())
            .addedAt(item.getCreatedAt())
            .productName(product != null ? product.name() : null)
            .productPrice(product != null ? product.price() : null)
            .productStatus(product != null ? product.status() : null)
            .productImageUrl(product != null ? product.imageUrl() : null)
            .build();
    }
}

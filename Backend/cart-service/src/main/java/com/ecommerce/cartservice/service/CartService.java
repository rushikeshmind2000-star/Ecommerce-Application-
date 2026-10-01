package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductClient;
import com.ecommerce.cartservice.client.UserClient;
import com.ecommerce.cartservice.dto.CartItemDto;
import com.ecommerce.cartservice.dto.CartRequest;
import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.cartservice.dto.CheckoutRequest;
import com.ecommerce.cartservice.dto.CreateOrderRequest;
import com.ecommerce.cartservice.dto.OrderDTO;
import com.ecommerce.cartservice.dto.OrderItemRequest;
import com.ecommerce.cartservice.client.OrderClient;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.repository.CartItemRepository;
import com.ecommerce.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final OrderClient orderClient;

    @Transactional
    public CartResponse addToCart(CartRequest request) {
        // 1. Validate User
        try {
            userClient.getUser(request.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("User not found or user-service unavailable: " + request.getUserId());
        }

        // 2. Validate Product
        ProductClient.ProductResponse product;
        try {
            product = productClient.getProduct(request.getProductId());
        } catch (Exception e) {
            throw new RuntimeException("Product not found or product-service unavailable: " + request.getProductId());
        }

        // Check if product is active and in stock (basic check)
        if (product.stockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product: " + product.name());
        }

        // 3. Get or Create Cart
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseGet(() -> cartRepository.save(Cart.builder().userId(request.getUserId()).build()));

        // 4. Add or Update Cart Item
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            // Update price in case it changed
            item.setPrice(product.price());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .price(product.price())
                    .build();
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID userId, UUID productId, Integer quantity) {
        if (quantity <= 0) {
            return removeCartItem(userId, productId);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeCartItem(UUID userId, UUID productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        cartRepository.delete(cart);
    }

    @Transactional
    public OrderDTO checkout(CheckoutRequest request) {
        // 1. Get cart
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + request.getUserId()));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Prepare Order Items
        List<OrderItemRequest> orderItems = cart.getItems().stream().map(item -> {
            // Fetch product details (like name and SKU) from product-service
            ProductClient.ProductResponse product;
            try {
                product = productClient.getProduct(item.getProductId());
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch product details for ID: " + item.getProductId());
            }

            return OrderItemRequest.builder()
                    .productId(item.getProductId())
                    .productName(product.name())
                    .sku(product.sku() != null ? product.sku() : "UNKNOWN-SKU")
                    .quantity(item.getQuantity())
                    .unitPrice(item.getPrice()) // Use price recorded in cart
                    .build();
        }).collect(Collectors.toList());

        // 3. Prepare Order Request
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .items(orderItems)
                .build();

        // 4. Call Order Service
        OrderDTO order = orderClient.createOrder(orderRequest);

        // 5. Clear Cart on success
        cartRepository.delete(cart);

        return order;
    }

    private CartResponse mapToResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream().map(this::mapToItemDto).collect(Collectors.toList()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDto mapToItemDto(CartItem item) {
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}

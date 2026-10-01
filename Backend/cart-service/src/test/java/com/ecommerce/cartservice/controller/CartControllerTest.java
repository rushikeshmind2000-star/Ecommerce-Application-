package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.CartItemDto;
import com.ecommerce.cartservice.dto.CartRequest;
import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.cartservice.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer (MockMvc) tests for {@link CartController}.
 *
 * The service layer is mocked — these tests verify routing,
 * request/response serialisation, and HTTP status codes.
 */
@WebMvcTest(CartController.class)
@DisplayName("CartController Web Layer Tests")
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CartService cartService;

    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID CART_ID    = UUID.randomUUID();

    private CartResponse buildCartResponse() {
        CartItemDto item = CartItemDto.builder()
            .id(UUID.randomUUID())
            .productId(PRODUCT_ID)
            .quantity(2)
            .price(new BigDecimal("74999"))
            .build();

        return CartResponse.builder()
            .id(CART_ID)
            .userId(USER_ID)
            .items(List.of(item))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    // ── POST /api/cart ─────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/cart")
    class AddToCartEndpoint {

        @Test
        @DisplayName("should return 201 CREATED with cart response")
        void addToCart_valid_returns201() throws Exception {
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(2)
                .build();

            when(cartService.addToCart(any())).thenReturn(buildCartResponse());

            mockMvc.perform(post("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when userId is missing")
        void addToCart_missingUserId_returns400() throws Exception {
            CartRequest request = CartRequest.builder()
                .productId(PRODUCT_ID).quantity(1)
                .build(); // userId is null

            mockMvc.perform(post("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when quantity is 0")
        void addToCart_zeroQuantity_returns400() throws Exception {
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(0)
                .build();

            mockMvc.perform(post("/api/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    // ── GET /api/cart/{userId} ─────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/cart/{userId}")
    class GetCartEndpoint {

        @Test
        @DisplayName("should return 200 OK with cart")
        void getCart_exists_returns200() throws Exception {
            when(cartService.getCartByUserId(USER_ID)).thenReturn(buildCartResponse());

            mockMvc.perform(get("/api/cart/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CART_ID.toString()))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()));
        }

        @Test
        @DisplayName("should propagate RuntimeException when cart not found (no global handler in cart-service)")
        void getCart_notFound_propagatesException() {
            when(cartService.getCartByUserId(USER_ID))
                .thenThrow(new RuntimeException("Cart not found for user: " + USER_ID));

            // cart-service has no GlobalExceptionHandler, so RuntimeException propagates
            // through MockMvc as a NestedServletException — assert on root cause
            assertThatThrownBy(() ->
                mockMvc.perform(get("/api/cart/{userId}", USER_ID)).andReturn()
            ).hasRootCauseInstanceOf(RuntimeException.class)
             .hasRootCauseMessage("Cart not found for user: " + USER_ID);
        }
    }

    // ── PUT /api/cart/{userId}/items/{productId} ────────────────────────────────
    @Nested
    @DisplayName("PUT /api/cart/{userId}/items/{productId}")
    class UpdateQuantityEndpoint {

        @Test
        @DisplayName("should return 200 OK with updated cart")
        void updateQuantity_valid_returns200() throws Exception {
            when(cartService.updateItemQuantity(eq(USER_ID), eq(PRODUCT_ID), eq(5)))
                .thenReturn(buildCartResponse());

            mockMvc.perform(put("/api/cart/{userId}/items/{productId}", USER_ID, PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("quantity", 5))))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should propagate IllegalArgumentException when quantity field is missing (no global handler)")
        void updateQuantity_missingQuantity_propagatesException() {
            // cart-service has no GlobalExceptionHandler; the manual null-check in the
            // controller throws IllegalArgumentException which propagates as NestedServletException
            assertThatThrownBy(() ->
                mockMvc.perform(put("/api/cart/{userId}/items/{productId}", USER_ID, PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // no quantity key
                    .andReturn()
            ).hasRootCauseInstanceOf(IllegalArgumentException.class)
             .hasRootCauseMessage("Quantity is required");
        }
    }

    // ── DELETE /api/cart/{userId}/items/{productId} ─────────────────────────────
    @Nested
    @DisplayName("DELETE /api/cart/{userId}/items/{productId}")
    class RemoveItemEndpoint {

        @Test
        @DisplayName("should return 200 OK after removing item")
        void removeItem_exists_returns200() throws Exception {
            when(cartService.removeCartItem(USER_ID, PRODUCT_ID)).thenReturn(buildCartResponse());

            mockMvc.perform(delete("/api/cart/{userId}/items/{productId}", USER_ID, PRODUCT_ID))
                .andExpect(status().isOk());

            verify(cartService).removeCartItem(USER_ID, PRODUCT_ID);
        }
    }

    // ── DELETE /api/cart/{userId} ───────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/cart/{userId}")
    class ClearCartEndpoint {

        @Test
        @DisplayName("should return 204 NO_CONTENT after clearing cart")
        void clearCart_exists_returns204() throws Exception {
            doNothing().when(cartService).clearCart(USER_ID);

            mockMvc.perform(delete("/api/cart/{userId}", USER_ID))
                .andExpect(status().isNoContent());

            verify(cartService).clearCart(USER_ID);
        }
    }
}

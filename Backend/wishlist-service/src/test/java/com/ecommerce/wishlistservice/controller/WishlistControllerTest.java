package com.ecommerce.wishlistservice.controller;

import com.ecommerce.wishlistservice.dto.AddWishlistRequest;
import com.ecommerce.wishlistservice.dto.WishlistItemResponse;
import com.ecommerce.wishlistservice.dto.WishlistResponse;
import com.ecommerce.wishlistservice.exception.DuplicateWishlistItemException;
import com.ecommerce.wishlistservice.exception.GlobalExceptionHandler;
import com.ecommerce.wishlistservice.exception.WishlistItemNotFoundException;
import com.ecommerce.wishlistservice.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer (MockMvc) tests for {@link WishlistController}.
 *
 * Verifies HTTP status codes, request validation, and JSON response shapes.
 */
@WebMvcTest(WishlistController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("WishlistController Web Layer Tests")
class WishlistControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private WishlistService wishlistService;

    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID ITEM_ID    = UUID.randomUUID();

    private WishlistItemResponse buildItemResponse() {
        return WishlistItemResponse.builder()
            .id(ITEM_ID)
            .userId(USER_ID)
            .productId(PRODUCT_ID)
            .addedAt(LocalDateTime.now())
            .productName("Samsung Galaxy S25")
            .productPrice(new BigDecimal("74999"))
            .productStatus("ACTIVE")
            .build();
    }

    private WishlistResponse buildWishlistResponse() {
        return WishlistResponse.builder()
            .userId(USER_ID)
            .totalItems(1)
            .items(List.of(buildItemResponse()))
            .build();
    }

    // ── POST /api/wishlist ──────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/wishlist")
    class AddToWishlistEndpoint {

        @Test
        @DisplayName("should return 201 CREATED with wishlist item response")
        void addToWishlist_valid_returns201() throws Exception {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(wishlistService.addToWishlist(any())).thenReturn(buildItemResponse());

            mockMvc.perform(post("/api/wishlist")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.productName").value("Samsung Galaxy S25"))
                .andExpect(jsonPath("$.productPrice").value(74999));
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when userId is missing")
        void addToWishlist_missingUserId_returns400() throws Exception {
            // productId only, no userId
            mockMvc.perform(post("/api/wishlist")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"productId\":\"" + PRODUCT_ID + "\"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when productId is missing")
        void addToWishlist_missingProductId_returns400() throws Exception {
            mockMvc.perform(post("/api/wishlist")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userId\":\"" + USER_ID + "\"}"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 409 CONFLICT when product is already in wishlist")
        void addToWishlist_duplicate_returns409() throws Exception {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(wishlistService.addToWishlist(any()))
                .thenThrow(new DuplicateWishlistItemException("Product is already in your wishlist."));

            mockMvc.perform(post("/api/wishlist")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
        }

        @Test
        @DisplayName("should return 404 NOT_FOUND when user does not exist")
        void addToWishlist_userNotFound_returns404() throws Exception {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(wishlistService.addToWishlist(any()))
                .thenThrow(new WishlistItemNotFoundException("User not found with id: " + USER_ID));

            mockMvc.perform(post("/api/wishlist")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        }

        @Test
        @DisplayName("should return 404 NOT_FOUND when product does not exist")
        void addToWishlist_productNotFound_returns404() throws Exception {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(wishlistService.addToWishlist(any()))
                .thenThrow(new WishlistItemNotFoundException("Product not found with id: " + PRODUCT_ID));

            mockMvc.perform(post("/api/wishlist")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    // ── GET /api/wishlist/{userId} ──────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/wishlist/{userId}")
    class GetWishlistEndpoint {

        @Test
        @DisplayName("should return 200 OK with full wishlist")
        void getWishlist_exists_returns200() throws Exception {
            when(wishlistService.getWishlist(USER_ID)).thenReturn(buildWishlistResponse());

            mockMvc.perform(get("/api/wishlist/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("Samsung Galaxy S25"));
        }

        @Test
        @DisplayName("should return 200 OK with empty items list")
        void getWishlist_empty_returnsEmptyList() throws Exception {
            WishlistResponse empty = WishlistResponse.builder()
                .userId(USER_ID).totalItems(0).items(List.of()).build();
            when(wishlistService.getWishlist(USER_ID)).thenReturn(empty);

            mockMvc.perform(get("/api/wishlist/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
        }
    }

    // ── DELETE /api/wishlist/{userId}/{productId} ───────────────────────────────
    @Nested
    @DisplayName("DELETE /api/wishlist/{userId}/{productId}")
    class RemoveFromWishlistEndpoint {

        @Test
        @DisplayName("should return 204 NO_CONTENT after successful removal")
        void removeFromWishlist_exists_returns204() throws Exception {
            doNothing().when(wishlistService).removeFromWishlist(USER_ID, PRODUCT_ID);

            mockMvc.perform(delete("/api/wishlist/{userId}/{productId}", USER_ID, PRODUCT_ID))
                .andExpect(status().isNoContent());

            verify(wishlistService).removeFromWishlist(USER_ID, PRODUCT_ID);
        }

        @Test
        @DisplayName("should return 404 NOT_FOUND when product not in wishlist")
        void removeFromWishlist_notFound_returns404() throws Exception {
            doThrow(new WishlistItemNotFoundException("Product is not in the wishlist for user " + USER_ID))
                .when(wishlistService).removeFromWishlist(USER_ID, PRODUCT_ID);

            mockMvc.perform(delete("/api/wishlist/{userId}/{productId}", USER_ID, PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }
}

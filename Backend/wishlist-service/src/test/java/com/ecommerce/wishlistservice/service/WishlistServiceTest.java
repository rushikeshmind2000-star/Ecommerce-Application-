package com.ecommerce.wishlistservice.service;

import com.ecommerce.wishlistservice.client.ProductClient;
import com.ecommerce.wishlistservice.client.UserClient;
import com.ecommerce.wishlistservice.dto.AddWishlistRequest;
import com.ecommerce.wishlistservice.dto.WishlistItemResponse;
import com.ecommerce.wishlistservice.dto.WishlistResponse;
import com.ecommerce.wishlistservice.entity.WishlistItem;
import com.ecommerce.wishlistservice.exception.DuplicateWishlistItemException;
import com.ecommerce.wishlistservice.exception.WishlistItemNotFoundException;
import com.ecommerce.wishlistservice.repository.WishlistRepository;
import com.ecommerce.wishlistservice.service.impl.WishlistServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WishlistServiceImpl}.
 *
 * All external dependencies (ProductClient, UserClient, WishlistRepository)
 * are mocked — no database or network required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService Unit Tests")
class WishlistServiceTest {

    @Mock private WishlistRepository wishlistRepository;
    @Mock private ProductClient productClient;
    @Mock private UserClient userClient;

    @InjectMocks private WishlistServiceImpl wishlistService;

    // ─── Fixtures ──────────────────────────────────────────────────────────────
    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID ITEM_ID    = UUID.randomUUID();

    private ProductClient.ProductResponse activeProduct;
    private ProductClient.ProductResponse inactiveProduct;
    private UserClient.UserResponse userResponse;
    private WishlistItem savedItem;

    @BeforeEach
    void setUp() {
        activeProduct = new ProductClient.ProductResponse(
            PRODUCT_ID, "Samsung Galaxy S25", "SAM-S25-256", new BigDecimal("74999"), 5, "ACTIVE", "https://img.example.com/s25.jpg"
        );
        inactiveProduct = new ProductClient.ProductResponse(
            PRODUCT_ID, "Old Phone", "OLD-SKU-123", new BigDecimal("9999"), 0, "INACTIVE", null
        );
        userResponse = new UserClient.UserResponse(USER_ID, "testuser", "test@gmail.com");
        savedItem = WishlistItem.builder()
            .id(ITEM_ID)
            .userId(USER_ID)
            .productId(PRODUCT_ID)
            .createdAt(LocalDateTime.now())
            .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // addToWishlist
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addToWishlist()")
    class AddToWishlistTests {

        @Test
        @DisplayName("should save wishlist item and return response when request is valid")
        void addToWishlist_valid_savesAndReturns() {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);

            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(activeProduct);
            when(wishlistRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(false);
            when(wishlistRepository.save(any(WishlistItem.class))).thenReturn(savedItem);

            WishlistItemResponse response = wishlistService.addToWishlist(request);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(response.getProductName()).isEqualTo("Samsung Galaxy S25");
            assertThat(response.getProductPrice()).isEqualByComparingTo("74999");
            verify(wishlistRepository).save(any(WishlistItem.class));
        }

        @Test
        @DisplayName("should throw WishlistItemNotFoundException when user does not exist")
        void addToWishlist_userNotFound_throwsNotFoundException() {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(userClient.getUser(USER_ID)).thenThrow(FeignException.NotFound.class);

            assertThatThrownBy(() -> wishlistService.addToWishlist(request))
                .isInstanceOf(WishlistItemNotFoundException.class)
                .hasMessageContaining("User not found");

            verify(wishlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw WishlistItemNotFoundException when product does not exist")
        void addToWishlist_productNotFound_throwsNotFoundException() {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenThrow(FeignException.NotFound.class);

            assertThatThrownBy(() -> wishlistService.addToWishlist(request))
                .isInstanceOf(WishlistItemNotFoundException.class)
                .hasMessageContaining("Product not found");

            verify(wishlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalStateException when product status is INACTIVE")
        void addToWishlist_inactiveProduct_throwsIllegalState() {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(inactiveProduct);

            assertThatThrownBy(() -> wishlistService.addToWishlist(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available")
                .hasMessageContaining("INACTIVE");

            verify(wishlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicateWishlistItemException when product already in wishlist")
        void addToWishlist_duplicate_throwsDuplicateException() {
            AddWishlistRequest request = new AddWishlistRequest(USER_ID, PRODUCT_ID);
            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(activeProduct);
            when(wishlistRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(true);

            assertThatThrownBy(() -> wishlistService.addToWishlist(request))
                .isInstanceOf(DuplicateWishlistItemException.class)
                .hasMessageContaining("already in your wishlist");

            verify(wishlistRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getWishlist
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getWishlist()")
    class GetWishlistTests {

        @Test
        @DisplayName("should return wishlist enriched with product details")
        void getWishlist_withItems_returnsEnrichedList() {
            when(wishlistRepository.findByUserId(USER_ID)).thenReturn(List.of(savedItem));
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(activeProduct);

            WishlistResponse response = wishlistService.getWishlist(USER_ID);

            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getProductName()).isEqualTo("Samsung Galaxy S25");
        }

        @Test
        @DisplayName("should return empty wishlist when user has no items")
        void getWishlist_empty_returnsEmptyList() {
            when(wishlistRepository.findByUserId(USER_ID)).thenReturn(List.of());

            WishlistResponse response = wishlistService.getWishlist(USER_ID);

            assertThat(response.getTotalItems()).isZero();
            assertThat(response.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should still return item (with null product details) when product-service is down")
        void getWishlist_productServiceDown_returnsItemWithNullDetails() {
            when(wishlistRepository.findByUserId(USER_ID)).thenReturn(List.of(savedItem));
            when(productClient.getProduct(PRODUCT_ID)).thenThrow(mock(FeignException.class));

            WishlistResponse response = wishlistService.getWishlist(USER_ID);

            // Graceful degradation — item is still in list, but product details are null
            assertThat(response.getTotalItems()).isEqualTo(1);
            assertThat(response.getItems().get(0).getProductName()).isNull();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // removeFromWishlist
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("removeFromWishlist()")
    class RemoveFromWishlistTests {

        @Test
        @DisplayName("should delete item when it exists")
        void removeFromWishlist_exists_deletesItem() {
            when(wishlistRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(true);

            wishlistService.removeFromWishlist(USER_ID, PRODUCT_ID);

            verify(wishlistRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
        }

        @Test
        @DisplayName("should throw WishlistItemNotFoundException when item not in wishlist")
        void removeFromWishlist_notFound_throwsNotFoundException() {
            when(wishlistRepository.existsByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(false);

            assertThatThrownBy(() -> wishlistService.removeFromWishlist(USER_ID, PRODUCT_ID))
                .isInstanceOf(WishlistItemNotFoundException.class)
                .hasMessageContaining("not in the wishlist");

            verify(wishlistRepository, never()).deleteByUserIdAndProductId(any(), any());
        }
    }
}

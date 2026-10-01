package com.ecommerce.cartservice.service;

import com.ecommerce.cartservice.client.ProductClient;
import com.ecommerce.cartservice.client.UserClient;
import com.ecommerce.cartservice.dto.CartRequest;
import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.cartservice.entity.Cart;
import com.ecommerce.cartservice.entity.CartItem;
import com.ecommerce.cartservice.repository.CartItemRepository;
import com.ecommerce.cartservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartService}.
 *
 * All external dependencies (ProductClient, UserClient, repositories) are mocked,
 * so no database or network is required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductClient productClient;
    @Mock private UserClient userClient;

    @InjectMocks private CartService cartService;

    // ─── Shared fixtures ───────────────────────────────────────────────────────
    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID CART_ID    = UUID.randomUUID();
    private static final UUID ITEM_ID    = UUID.randomUUID();

    private ProductClient.ProductResponse productResponse;
    private UserClient.UserResponse userResponse;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        productResponse = new ProductClient.ProductResponse(
            PRODUCT_ID, "Samsung Galaxy S25", "SAM-S25-256", new BigDecimal("74999"), 10, "ACTIVE"
        );
        userResponse = new UserClient.UserResponse(USER_ID, "testuser", "test@gmail.com");

        cart = Cart.builder()
            .id(CART_ID)
            .userId(USER_ID)
            .items(new ArrayList<>())
            .build();

        cartItem = CartItem.builder()
            .id(ITEM_ID)
            .cart(cart)
            .productId(PRODUCT_ID)
            .quantity(1)
            .price(new BigDecimal("74999"))
            .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // addToCart
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addToCart()")
    class AddToCartTests {

        @Test
        @DisplayName("should create a new cart and add item when user has no existing cart")
        void addToCart_newCart_createsCartAndItem() {
            // Arrange
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(2)
                .build();

            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(productResponse);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

            // Act
            CartResponse response = cartService.addToCart(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            verify(cartRepository, times(2)).save(any(Cart.class)); // once to create, once to save
        }

        @Test
        @DisplayName("should update quantity when product is already in cart")
        void addToCart_existingItem_updatesQuantity() {
            // Arrange
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(3)
                .build();

            cart.getItems().add(cartItem);

            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(productResponse);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
            when(cartRepository.save(any(Cart.class))).thenReturn(cart);

            // Act
            CartResponse response = cartService.addToCart(request);

            // Assert
            assertThat(response).isNotNull();
            // quantity should be updated: 1 (existing) + 3 (new) = 4
            assertThat(cartItem.getQuantity()).isEqualTo(4);
            verify(cartItemRepository).save(cartItem);
        }

        @Test
        @DisplayName("should throw RuntimeException when user is not found")
        void addToCart_userNotFound_throwsException() {
            // Arrange
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(1)
                .build();

            when(userClient.getUser(USER_ID)).thenThrow(new RuntimeException("404"));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(USER_ID.toString());
        }

        @Test
        @DisplayName("should throw RuntimeException when product is not found")
        void addToCart_productNotFound_throwsException() {
            // Arrange
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(1)
                .build();

            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenThrow(new RuntimeException("404"));

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(PRODUCT_ID.toString());
        }

        @Test
        @DisplayName("should throw RuntimeException when requested quantity exceeds stock")
        void addToCart_insufficientStock_throwsException() {
            // Arrange — product has only 10 stock, user wants 20
            CartRequest request = CartRequest.builder()
                .userId(USER_ID).productId(PRODUCT_ID).quantity(20)
                .build();

            when(userClient.getUser(USER_ID)).thenReturn(userResponse);
            when(productClient.getProduct(PRODUCT_ID)).thenReturn(productResponse); // stock = 10

            // Act & Assert
            assertThatThrownBy(() -> cartService.addToCart(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getCartByUserId
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getCartByUserId()")
    class GetCartTests {

        @Test
        @DisplayName("should return cart when it exists")
        void getCartByUserId_exists_returnsCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            CartResponse response = cartService.getCartByUserId(USER_ID);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(CART_ID);
        }

        @Test
        @DisplayName("should throw RuntimeException when cart not found")
        void getCartByUserId_notFound_throwsException() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getCartByUserId(USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // updateItemQuantity
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateItemQuantity()")
    class UpdateItemQuantityTests {

        @Test
        @DisplayName("should update quantity successfully")
        void updateItemQuantity_valid_updatesItem() {
            cart.getItems().add(cartItem);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
            when(cartRepository.save(any())).thenReturn(cart);

            CartResponse response = cartService.updateItemQuantity(USER_ID, PRODUCT_ID, 5);

            assertThat(response).isNotNull();
            assertThat(cartItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should remove item when quantity is set to 0")
        void updateItemQuantity_zeroQuantity_removesItem() {
            cart.getItems().add(cartItem);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
            when(cartRepository.save(any())).thenReturn(cart);

            // quantity = 0 should trigger removeCartItem path
            CartResponse response = cartService.updateItemQuantity(USER_ID, PRODUCT_ID, 0);

            assertThat(response).isNotNull();
            verify(cartItemRepository).delete(cartItem);
        }

        @Test
        @DisplayName("should throw RuntimeException when cart not found")
        void updateItemQuantity_cartNotFound_throwsException() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateItemQuantity(USER_ID, PRODUCT_ID, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
        }

        @Test
        @DisplayName("should throw RuntimeException when item not in cart")
        void updateItemQuantity_itemNotInCart_throwsException() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateItemQuantity(USER_ID, PRODUCT_ID, 3))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // removeCartItem
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("removeCartItem()")
    class RemoveCartItemTests {

        @Test
        @DisplayName("should remove item from cart successfully")
        void removeCartItem_exists_removesItem() {
            cart.getItems().add(cartItem);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.of(cartItem));
            when(cartRepository.save(any())).thenReturn(cart);

            CartResponse response = cartService.removeCartItem(USER_ID, PRODUCT_ID);

            assertThat(response).isNotNull();
            verify(cartItemRepository).delete(cartItem);
            assertThat(cart.getItems()).doesNotContain(cartItem);
        }

        @Test
        @DisplayName("should throw RuntimeException when item not in cart")
        void removeCartItem_itemNotFound_throwsException() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeCartItem(USER_ID, PRODUCT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // clearCart
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("clearCart()")
    class ClearCartTests {

        @Test
        @DisplayName("should delete cart successfully")
        void clearCart_exists_deletesCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            cartService.clearCart(USER_ID);

            verify(cartRepository).delete(cart);
        }

        @Test
        @DisplayName("should throw RuntimeException when cart not found")
        void clearCart_notFound_throwsException() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.clearCart(USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cart not found");
        }
    }
}

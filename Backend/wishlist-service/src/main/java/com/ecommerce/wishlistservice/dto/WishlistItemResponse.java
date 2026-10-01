package com.ecommerce.wishlistservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Response body for a single wishlist item — includes product details fetched via Feign */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {

    private UUID id;
    private UUID userId;
    private UUID productId;
    private LocalDateTime addedAt;

    // Product details (fetched from product-service)
    private String productName;
    private BigDecimal productPrice;
    private String productStatus;
    private String productImageUrl;
}

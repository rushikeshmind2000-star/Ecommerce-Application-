package com.ecommerce.wishlistservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Request body for adding an item to the wishlist */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddWishlistRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotNull(message = "productId is required")
    private UUID productId;
}

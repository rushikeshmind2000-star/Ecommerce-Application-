package com.ecommerce.wishlistservice.repository;

import com.ecommerce.wishlistservice.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, UUID> {

    /** Get all wishlist items for a user */
    List<WishlistItem> findByUserId(UUID userId);

    /** Check if a product is already in the user's wishlist */
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);

    /** Remove a specific product from the user's wishlist */
    void deleteByUserIdAndProductId(UUID userId, UUID productId);
}

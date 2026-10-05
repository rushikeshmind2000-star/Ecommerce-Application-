package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    /** All approved reviews for a product (customer storefront) */
    Page<ProductReview> findByProductIdAndApprovedTrue(UUID productId, Pageable pageable);

    /** All reviews for a product (admin view) */
    Page<ProductReview> findByProductId(UUID productId, Pageable pageable);

    /** All reviews written by a specific user */
    Page<ProductReview> findByUserId(UUID userId, Pageable pageable);

    /** Check if a user already reviewed this product */
    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    /** Fetch user's specific review for a product */
    Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);

    /** Average rating for a product (used to update product aggregate) */
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.approved = true")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    /** Count approved reviews for a product */
    long countByProductIdAndApprovedTrue(UUID productId);
}

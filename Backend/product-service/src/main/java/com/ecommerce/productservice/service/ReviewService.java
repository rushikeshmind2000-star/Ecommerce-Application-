package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.CreateReviewRequest;
import com.ecommerce.productservice.dto.ReviewDTO;
import com.ecommerce.productservice.dto.ReviewSummaryDTO;
import com.ecommerce.productservice.entity.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    /**
     * Submit a new review for a product.
     * Enforces exactly one review per user per product.
     */
    ReviewDTO createReview(UUID productId, CreateReviewRequest request);

    /**
     * Update an existing review.
     */
    ReviewDTO updateReview(UUID reviewId, UUID userId, CreateReviewRequest request);

    /**
     * Delete a review.
     */
    void deleteReview(UUID reviewId, UUID userId);

    /**
     * Fetch reviews for a product (customer storefront), includes current user's reaction if viewerId is provided.
     */
    Page<ReviewDTO> getProductReviews(UUID productId, UUID viewerId, Pageable pageable);

    /**
     * Get review summary (avg rating, star breakdown) for a product.
     */
    ReviewSummaryDTO getProductReviewSummary(UUID productId);

    /**
     * Add or update a like/dislike reaction on a review.
     */
    void reactToReview(UUID reviewId, UUID userId, ReactionType type);

    /**
     * Remove a reaction from a review.
     */
    void removeReaction(UUID reviewId, UUID userId);
}

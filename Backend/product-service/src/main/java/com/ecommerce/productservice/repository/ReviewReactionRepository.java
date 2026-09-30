package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.ReactionType;
import com.ecommerce.productservice.entity.ReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, UUID> {

    Optional<ReviewReaction> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    long countByReviewIdAndType(UUID reviewId, ReactionType type);

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    /** Find the current reaction type of a user for a specific review */
    @Query("SELECT r.type FROM ReviewReaction r WHERE r.review.id = :reviewId AND r.userId = :userId")
    Optional<ReactionType> findTypeByReviewIdAndUserId(@Param("reviewId") UUID reviewId, @Param("userId") UUID userId);
}

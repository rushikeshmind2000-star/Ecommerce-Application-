package com.ecommerce.productservice.dto;

import com.ecommerce.productservice.entity.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full review response returned to the client.
 * Includes image list, aggregate reaction counts, and the calling user's own reaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private UUID id;
    private UUID productId;
    private UUID userId;
    private String userName;
    private int rating;
    private String title;
    private String body;
    private boolean verifiedPurchase;
    private boolean approved;

    /** Images attached to this review */
    private List<ReviewImageDTO> images;

    /** Total LIKE count */
    private long likes;

    /** Total DISLIKE count */
    private long dislikes;

    /**
     * The reaction the currently authenticated user has given to THIS review.
     * Null if the user has not reacted, or if this is the user's own review.
     */
    private ReactionType myReaction;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.ecommerce.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks a single user's LIKE or DISLIKE on another user's review.
 * A user may only have one reaction per review (enforced by unique constraint).
 * Changing reaction type (LIKE → DISLIKE) updates the existing row.
 */
@Entity
@Table(
    name = "review_reactions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_reaction_user_review",
        columnNames = {"user_id", "review_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview review;

    /** User who reacted */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

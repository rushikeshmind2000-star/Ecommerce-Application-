package com.ecommerce.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A customer review on a product.
 * Supports: text body, 1-5 star rating, multiple images, and like/dislike votes.
 */
@Entity
@Table(
    name = "product_reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_review_user_product",
        columnNames = {"user_id", "product_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Product being reviewed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** ID of the customer who wrote this review */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Display name stored at write time (denormalised for performance) */
    @Column(name = "user_name", nullable = false)
    private String userName;

    /** 1 – 5 stars */
    @Column(nullable = false)
    private int rating;

    /** Review headline / title */
    @Column(length = 200)
    private String title;

    /** Full review text */
    @Column(columnDefinition = "TEXT")
    private String body;

    /** Whether the admin has verified this review (e.g. verified purchase) */
    @Column(name = "verified_purchase")
    @Builder.Default
    private boolean verifiedPurchase = false;

    /** Soft-delete / moderation flag */
    @Column(nullable = false)
    @Builder.Default
    private boolean approved = true;

    /** Images attached to this review */
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> images = new ArrayList<>();

    /** Like / dislike reactions from other users */
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewReaction> reactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

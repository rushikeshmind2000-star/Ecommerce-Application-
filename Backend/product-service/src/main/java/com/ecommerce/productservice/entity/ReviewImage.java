package com.ecommerce.productservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * An image attached to a product review.
 */
@Entity
@Table(name = "review_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview review;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    /** Human-readable caption for the image */
    @Column(length = 300)
    private String caption;

    /** Display order */
    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;
}

package com.ecommerce.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary returned at the product level: average rating + breakdown per star.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {
    private double averageRating;
    private long totalReviews;
    private long fiveStar;
    private long fourStar;
    private long threeStar;
    private long twoStar;
    private long oneStar;
}

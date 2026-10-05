package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Request body for creating or updating a product review.
 */
@Data
public class CreateReviewRequest {

    @NotNull(message = "User ID is mandatory")
    private UUID userId;

    @NotBlank(message = "User name is mandatory")
    private String userName;

    @NotNull(message = "Rating is mandatory")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Review body must not exceed 5000 characters")
    private String body;

    /** Up to 10 image URLs (e.g. uploaded to S3 / Cloudinary before calling this API) */
    @Size(max = 10, message = "You can attach at most 10 images per review")
    private List<ReviewImageRequest> images;
}

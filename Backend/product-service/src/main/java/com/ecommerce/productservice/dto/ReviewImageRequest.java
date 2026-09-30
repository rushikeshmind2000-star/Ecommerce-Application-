package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * A single image attached to a review.
 * The actual file should be uploaded to object storage first;
 * this DTO carries only the resulting URL and an optional caption.
 */
@Data
public class ReviewImageRequest {

    @NotBlank(message = "Image URL is mandatory")
    private String imageUrl;

    @Size(max = 300, message = "Caption must not exceed 300 characters")
    private String caption;

    private int sortOrder = 0;
}

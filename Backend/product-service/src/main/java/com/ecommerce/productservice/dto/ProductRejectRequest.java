package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

/**
 * Request body for admin to reject a product with a mandatory reason.
 */
@Data
public class ProductRejectRequest {

    @NotBlank(message = "Rejection reason is mandatory")
    private String reason;

    /** Admin user ID performing the action (can come from JWT in production) */
    private UUID adminId;
}

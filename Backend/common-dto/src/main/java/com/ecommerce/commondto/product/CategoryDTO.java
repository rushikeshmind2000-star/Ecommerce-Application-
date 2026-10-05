package com.ecommerce.commondto.product;

import com.ecommerce.commondto.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO representing a product category.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private UUID id;
    private String name;
    private String description;
    private CategoryStatus status;
}

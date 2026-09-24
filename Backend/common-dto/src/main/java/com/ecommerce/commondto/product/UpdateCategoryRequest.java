package com.ecommerce.commondto.product;

import com.ecommerce.commondto.enums.CategoryStatus;
import lombok.Data;

/**
 * Request DTO to update an existing product category.
 */
@Data
public class UpdateCategoryRequest {

    private String name;
    private String description;
    private CategoryStatus status;
}

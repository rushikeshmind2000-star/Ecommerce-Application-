package com.ecommerce.commondto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO to create a new product category.
 */
@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is mandatory")
    private String name;

    private String description;
}

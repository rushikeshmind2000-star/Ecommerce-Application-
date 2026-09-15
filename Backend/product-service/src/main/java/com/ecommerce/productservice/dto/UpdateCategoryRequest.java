package com.ecommerce.productservice.dto;

import com.ecommerce.productservice.entity.CategoryStatus;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String name;
    private String description;
    private CategoryStatus status;
}

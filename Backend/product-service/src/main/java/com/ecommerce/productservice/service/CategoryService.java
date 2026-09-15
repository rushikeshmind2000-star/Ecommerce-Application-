package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.CategoryDTO;
import com.ecommerce.productservice.dto.CreateCategoryRequest;
import com.ecommerce.productservice.dto.UpdateCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryDTO createCategory(CreateCategoryRequest request);
    List<CategoryDTO> getAllCategories();
    CategoryDTO getCategoryById(UUID id);
    CategoryDTO updateCategory(UUID id, UpdateCategoryRequest request);
    void deleteCategory(UUID id);
}

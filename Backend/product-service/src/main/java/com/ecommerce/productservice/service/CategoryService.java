package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CategoryRequestDto;
import com.ecommerce.productservice.dto.response.CategoryResponseDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto);

    List<CategoryResponseDto> getAllCategories();

    CategoryResponseDto getCategoryById(UUID id);
}

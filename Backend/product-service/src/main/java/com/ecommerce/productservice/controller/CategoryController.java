package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.CategoryRequestDto;
import com.ecommerce.productservice.dto.response.CategoryResponseDto;
import com.ecommerce.productservice.enums.ApiKey;
import com.ecommerce.productservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Map<ApiKey, Object>> createCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto) {
        CategoryResponseDto createdCategory = categoryService.createCategory(categoryRequestDto);
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Category created successfully");
        response.put(ApiKey.data, createdCategory);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<ApiKey, Object>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Categories fetched successfully");
        response.put(ApiKey.data, categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<ApiKey, Object>> getCategoryById(@PathVariable UUID id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Category fetched successfully");
        response.put(ApiKey.data, category);
        return ResponseEntity.ok(response);
    }
}

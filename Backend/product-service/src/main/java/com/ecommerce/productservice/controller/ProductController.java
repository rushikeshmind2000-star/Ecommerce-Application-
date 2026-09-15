package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.ProductRequestDto;
import com.ecommerce.productservice.dto.request.ProductUpdateRequestDto;
import com.ecommerce.productservice.dto.response.ProductResponseDto;
import com.ecommerce.productservice.enums.ApiKey;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Map<ApiKey, Object>> createProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
        ProductResponseDto createdProduct = productService.createProduct(productRequestDto);
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Product created successfully");
        response.put(ApiKey.data, createdProduct);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<ApiKey, Object>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Products fetched successfully");
        response.put(ApiKey.data, products);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<ApiKey, Object>> getProductById(@PathVariable UUID id) {
        ProductResponseDto product = productService.getProductById(id);
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Product fetched successfully");
        response.put(ApiKey.data, product);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Map<ApiKey, Object>> updateProduct(@Valid @RequestBody ProductUpdateRequestDto productUpdateRequestDto) {
        ProductResponseDto updatedProduct = productService.updateProduct(productUpdateRequestDto);
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Product updated successfully");
        response.put(ApiKey.data, updatedProduct);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<ApiKey, Object>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, true);
        response.put(ApiKey.message, "Product deleted successfully");
        return ResponseEntity.ok(response);
    }
}

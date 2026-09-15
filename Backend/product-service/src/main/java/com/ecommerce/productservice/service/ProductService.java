package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.ProductRequestDto;
import com.ecommerce.productservice.dto.request.ProductUpdateRequestDto;
import com.ecommerce.productservice.dto.response.ProductResponseDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto productRequestDto);

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProductById(UUID id);

    ProductResponseDto updateProduct(ProductUpdateRequestDto productUpdateRequestDto);

    void deleteProduct(UUID id);
}

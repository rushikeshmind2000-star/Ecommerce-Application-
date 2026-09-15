package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.dto.UpdateProductRequest;
import com.ecommerce.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductDTO createProduct(CreateProductRequest request);
    Page<ProductDTO> getProducts(Pageable pageable);
    ProductDTO getProductById(UUID id);
    ProductDTO updateProduct(UUID id, UpdateProductRequest request);
    void deleteProduct(UUID id);
    void updateProductStatus(UUID id, ProductStatus status);
    ProductDTO getProductBySku(String sku);
    Page<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable);
}

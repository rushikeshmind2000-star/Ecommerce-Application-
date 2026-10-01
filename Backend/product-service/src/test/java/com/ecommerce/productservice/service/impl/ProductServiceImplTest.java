package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.dto.*;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductImage;
import com.ecommerce.productservice.entity.ProductStatus;
import com.ecommerce.productservice.exception.DuplicateResourceException;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Category category;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");

        product = Product.builder()
                .id(productId)
                .category(category)
                .name("Test Product")
                .sku("SKU-123")
                .price(BigDecimal.valueOf(100.0))
                .status(ProductStatus.PENDING)
                .images(new ArrayList<>())
                .build();
    }

    @Test
    void createProduct_Success() {
        CreateProductRequest request = new CreateProductRequest();
        request.setCategoryId(categoryId);
        request.setSku("SKU-123");
        request.setName("Test Product");
        request.setPrice(BigDecimal.valueOf(100.0));
        
        when(productRepository.existsBySku(any())).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("SKU-123", result.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateSku_ThrowsException() {
        CreateProductRequest request = new CreateProductRequest();
        request.setSku("SKU-123");
        
        when(productRepository.existsBySku(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void approveProduct_Success() {
        UUID adminId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.approveProduct(productId, adminId);

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(adminId, product.getReviewedBy());
        assertNotNull(product.getReviewedAt());
        verify(productRepository).save(product);
    }
    
    @Test
    void rejectProduct_Success() {
        UUID adminId = UUID.randomUUID();
        ProductRejectRequest request = new ProductRejectRequest();
        request.setAdminId(adminId);
        request.setReason("Incomplete details");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.rejectProduct(productId, request);

        assertEquals(ProductStatus.REJECTED, product.getStatus());
        assertEquals("Incomplete details", product.getRejectionReason());
        verify(productRepository).save(product);
    }

    @Test
    void getProducts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductDTO> result = productService.getProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateProductStatus_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.updateProductStatus(productId, ProductStatus.ACTIVE);

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteProduct(productId));
        verify(productRepository).deleteById(productId);
    }
}

package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.dto.ProductRejectRequest;
import com.ecommerce.productservice.dto.UpdateProductRequest;
import com.ecommerce.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductDTO createProduct(CreateProductRequest request);

    Page<ProductDTO> getProducts(Pageable pageable);

    /** Returns only ACTIVE products — used by the customer storefront */
    Page<ProductDTO> getActiveProducts(Pageable pageable);

    /** Returns all products filtered by status — used by admin */
    Page<ProductDTO> getProductsByStatus(ProductStatus status, Pageable pageable);

    /** Returns products belonging to a specific vendor — used by vendor portal */
    Page<ProductDTO> getProductsByVendor(UUID vendorId, Pageable pageable);

    ProductDTO getProductById(UUID id);

    ProductDTO updateProduct(UUID id, UpdateProductRequest request);

    void deleteProduct(UUID id);

    ProductDTO updateProductStatus(UUID id, ProductStatus status);

    ProductDTO getProductBySku(String sku);

    Page<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable);

    // ── Approval Workflow ────────────────────────────────────────────────────

    /**
     * Admin approves a pending product → status becomes ACTIVE and it becomes
     * visible to customers on the storefront.
     */
    ProductDTO approveProduct(UUID productId, UUID adminId);

    /**
     * Admin rejects a pending product with a mandatory reason.
     * Status becomes REJECTED. The reason is persisted so the vendor can read it.
     */
    ProductDTO rejectProduct(UUID productId, ProductRejectRequest request);
}

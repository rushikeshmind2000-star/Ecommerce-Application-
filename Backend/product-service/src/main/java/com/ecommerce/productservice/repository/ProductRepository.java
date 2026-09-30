package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    /** Storefront — only ACTIVE products visible to customers */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    /** Admin — filter all products by any status (PENDING_APPROVAL, REJECTED, etc.) */
    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    /** Vendor portal — all products submitted by this vendor regardless of status */
    Page<Product> findByVendorId(UUID vendorId, Pageable pageable);

    /** Vendor portal — products of this vendor filtered by status */
    Page<Product> findByVendorIdAndStatus(UUID vendorId, ProductStatus status, Pageable pageable);
}

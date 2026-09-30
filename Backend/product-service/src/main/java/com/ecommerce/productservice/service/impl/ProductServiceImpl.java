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
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ─────────────────────────────────────────────────────────────────
    // CREATE — Vendor submits a product (goes to PENDING_APPROVAL)
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory", "activeProducts"}, allEntries = true)
    public ProductDTO createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .brand(request.getBrand())
                .vendorId(request.getVendorId())
                // Always starts as PENDING — must be reviewed by admin
                .status(ProductStatus.PENDING)
                .build();

        if (request.getImages() != null) {
            request.getImages().forEach(imgReq -> {
                ProductImage img = ProductImage.builder()
                        .product(product)
                        .imageUrl(imgReq.getImageUrl())
                        .isPrimary(imgReq.isPrimary())
                        .build();
                product.getImages().add(img);
            });
        }

        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToDTO);
    }

    /** Customer storefront — only ACTIVE products */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activeProducts")
    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable).map(this::mapToDTO);
    }

    /** Admin — filter by any status */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByStatus(ProductStatus status, Pageable pageable) {
        return productRepository.findAllByStatus(status, pageable).map(this::mapToDTO);
    }

    /** Vendor portal — all their own products */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByVendor(UUID vendorId, Pageable pageable) {
        return productRepository.findByVendorId(vendorId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(UUID id) {
        return mapToDTO(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productBySku", key = "#sku")
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId, pageable).map(this::mapToDTO);
    }

    // ─────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @CachePut(value = "product", key = "#id")
    @CacheEvict(value = {"products", "productsByCategory", "productBySku", "activeProducts"}, allEntries = true)
    public ProductDTO updateProduct(UUID id, UpdateProductRequest request) {
        Product product = findOrThrow(id);

        if (request.getName() != null)        product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null)       product.setPrice(request.getPrice());
        if (request.getStatus() != null)      product.setStatus(request.getStatus());
        if (request.getBrand() != null)       product.setBrand(request.getBrand());

        if (request.getImages() != null) {
            product.getImages().clear();
            request.getImages().forEach(imgReq -> {
                ProductImage img = ProductImage.builder()
                        .product(product)
                        .imageUrl(imgReq.getImageUrl())
                        .isPrimary(imgReq.isPrimary())
                        .build();
                product.getImages().add(img);
            });
        }

        return mapToDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"product", "products", "productsByCategory", "productBySku", "activeProducts"}, allEntries = true)
    public ProductDTO updateProductStatus(UUID id, ProductStatus status) {
        Product product = findOrThrow(id);
        product.setStatus(status);
        return mapToDTO(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @CacheEvict(value = {"product", "products", "productsByCategory", "productBySku", "activeProducts"}, allEntries = true)
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────
    // APPROVAL WORKFLOW
    // ─────────────────────────────────────────────────────────────────

    /**
     * Admin approves a pending product.
     * The product status becomes ACTIVE and it becomes visible on the customer
     * storefront immediately (activeProducts cache is evicted).
     */
    @Override
    @Transactional
    @CacheEvict(value = {"product", "products", "activeProducts"}, allEntries = true)
    public ProductDTO approveProduct(UUID productId, UUID adminId) {
        Product product = findOrThrow(productId);

        if (product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalStateException(
                    "Only products with PENDING status can be approved. Current status: " + product.getStatus());
        }

        product.setStatus(ProductStatus.ACTIVE);
        product.setReviewedBy(adminId);
        product.setReviewedAt(LocalDateTime.now());
        product.setRejectionReason(null); // clear any previous rejection reason

        return mapToDTO(productRepository.save(product));
    }

    /**
     * Admin rejects a pending product with a mandatory reason.
     * The reason is persisted so the vendor can read it from their portal.
     */
    @Override
    @Transactional
    @CacheEvict(value = {"product", "products"}, allEntries = true)
    public ProductDTO rejectProduct(UUID productId, ProductRejectRequest request) {
        Product product = findOrThrow(productId);

        if (product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalStateException(
                    "Only products with PENDING status can be rejected. Current status: " + product.getStatus());
        }

        product.setStatus(ProductStatus.REJECTED);
        product.setRejectionReason(request.getReason());
        product.setReviewedBy(request.getAdminId());
        product.setReviewedAt(LocalDateTime.now());

        return mapToDTO(productRepository.save(product));
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────
    private Product findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .status(product.getStatus())
                .brand(product.getBrand())
                .vendorId(product.getVendorId())
                .reviewedBy(product.getReviewedBy())
                .reviewedAt(product.getReviewedAt())
                .rejectionReason(product.getRejectionReason())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .images(product.getImages().stream().map(img ->
                        ProductImageDTO.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .isPrimary(img.isPrimary())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}

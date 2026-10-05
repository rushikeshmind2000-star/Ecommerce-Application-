package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.CreateProductRequest;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.dto.ProductRejectRequest;
import com.ecommerce.productservice.dto.UpdateProductRequest;
import com.ecommerce.productservice.entity.ProductStatus;
import com.ecommerce.productservice.service.ProductService;
import com.ecommerce.productservice.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for product management.
 *
 * Approval workflow:
 *   Vendor submits  →  POST /api/products                       status = PENDING
 *   Admin lists     →  GET  /api/products?status=PENDING
 *   Admin approves  →  PATCH /api/products/{id}/approve
 *   Admin rejects   →  PATCH /api/products/{id}/reject          body: { reason, adminId }
 *   Vendor reads    →  GET  /api/products/vendor/{vendorId}     sees status + rejectionReason
 *   Storefront      →  GET  /api/products/active                ACTIVE only
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "🛍️ Products", description = "Product catalog management — vendor submission, admin approval workflow, customer storefront")
public class ProductController {

    private final ProductService productService;

    // ════════════════════════════════════════════════════════════════════
    //  VENDOR
    // ════════════════════════════════════════════════════════════════════

    @PostMapping
    @RequireRole("VENDOR")
    @Operation(
            summary = "Submit a new product (Vendor)",
            description = "Vendor submits a new product for admin review. Status is automatically set to **PENDING**. " +
                    "The product will NOT appear on the storefront until an Admin approves it. 🔒 Vendor role required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product submitted — awaiting admin approval"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "A product with this SKU already exists")
    })
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @GetMapping("/vendor/{vendorId}")
    @RequireRole("VENDOR")
    @Operation(
            summary = "Get vendor's own products",
            description = "Returns all products submitted by a specific vendor — including PENDING, ACTIVE, and REJECTED ones. " +
                    "Check the `status` and `rejectionReason` fields to see admin decisions. 🔒 Vendor role required."
    )
    @ApiResponse(responseCode = "200", description = "Vendor's products returned")
    public ResponseEntity<Page<ProductDTO>> getProductsByVendor(
            @Parameter(description = "Vendor UUID", required = true) @PathVariable UUID vendorId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc | desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(productService.getProductsByVendor(vendorId, PageRequest.of(page, size, sort)));
    }

    // ════════════════════════════════════════════════════════════════════
    //  CUSTOMER STOREFRONT
    // ════════════════════════════════════════════════════════════════════

    @GetMapping("/active")
    @Operation(
            summary = "Get active products (Customer Storefront)",
            description = "Returns only **ACTIVE** (admin-approved) products. This is the public-facing product list for customers. No auth required."
    )
    @ApiResponse(responseCode = "200", description = "Active products returned")
    public ResponseEntity<Page<ProductDTO>> getActiveProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc | desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(productService.getActiveProducts(PageRequest.of(page, size, sort)));
    }

    // ════════════════════════════════════════════════════════════════════
    //  ADMIN
    // ════════════════════════════════════════════════════════════════════

    @GetMapping
    @RequireRole("ADMIN")
    @Operation(
            summary = "Get all products (Admin)",
            description = "Returns all products. Filter by `status` to list only products awaiting review.\n\n" +
                    "**Example — list pending approvals:** `GET /api/products?status=PENDING` 🔒 Admin role required."
    )
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc | desc") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by status: PENDING | ACTIVE | INACTIVE | REJECTED | OUT_OF_STOCK") @RequestParam(required = false) ProductStatus status
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (status != null) {
            return ResponseEntity.ok(productService.getProductsByStatus(status, pageable));
        }
        return ResponseEntity.ok(productService.getProducts(pageable));
    }

    @PatchMapping("/{id}/approve")
    @RequireRole("ADMIN")
    @Operation(
            summary = "Approve a pending product (Admin)",
            description = "Sets product status to **ACTIVE** — product immediately appears on the customer storefront. " +
                    "Only works on products with status=PENDING. 🔒 Admin role required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product approved and is now ACTIVE"),
            @ApiResponse(responseCode = "400", description = "Product is not in PENDING status"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDTO> approveProduct(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Admin user UUID", required = true) @RequestParam UUID adminId) {
        return ResponseEntity.ok(productService.approveProduct(id, adminId));
    }

    @PatchMapping("/{id}/reject")
    @RequireRole("ADMIN")
    @Operation(
            summary = "Reject a pending product (Admin)",
            description = "Sets product status to **REJECTED** with a mandatory reason that the vendor can read. " +
                    "Only works on products with status=PENDING. 🔒 Admin role required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product rejected — vendor can see the reason"),
            @ApiResponse(responseCode = "400", description = "Rejection reason is mandatory / product not in PENDING status"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDTO> rejectProduct(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody ProductRejectRequest request) {
        return ResponseEntity.ok(productService.rejectProduct(id, request));
    }

    // ════════════════════════════════════════════════════════════════════
    //  COMMON
    // ════════════════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns a single product by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Returns a product by its unique Stock Keeping Unit code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "No product with this SKU")
    })
    public ResponseEntity<ProductDTO> getProductBySku(
            @Parameter(description = "SKU string, e.g. IP17-256-BLK", required = true) @PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Returns a paginated list of ACTIVE products in a category.")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID categoryId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc | desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, PageRequest.of(page, size, sort)));
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "VENDOR"})
    @Operation(summary = "Update product details", description = "Updates product name, description, price, brand or images. 🔒 Vendor (own product) / Admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}/status")
    @RequireRole("ADMIN")
    @Operation(summary = "Update product status (Admin)", description = "Manually change any product status field. 🔒 Admin role required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDTO> updateProductStatus(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "New status: ACTIVE | INACTIVE | PENDING | REJECTED | OUT_OF_STOCK", required = true)
            @RequestParam ProductStatus status) {
        return ResponseEntity.ok(productService.updateProductStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "VENDOR"})
    @Operation(summary = "Delete a product", description = "Permanently removes a product and all its images. 🔒 Vendor (own) / Admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}

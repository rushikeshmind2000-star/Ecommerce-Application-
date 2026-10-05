package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.CategoryDTO;
import com.ecommerce.productservice.dto.CreateCategoryRequest;
import com.ecommerce.productservice.dto.ProductDTO;
import com.ecommerce.productservice.dto.UpdateCategoryRequest;
import com.ecommerce.productservice.service.CategoryService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "🗂️ Categories", description = "Product category management — create, list, update and delete categories. 🔒 Create/Update/Delete require Admin role.")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    // ── Create Category (Admin) ───────────────────────────────────────────────
    @PostMapping
    @RequireRole("ADMIN")
    @Operation(summary = "Create a new category", description = "Creates a product category. 🔒 Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return new ResponseEntity<>(categoryService.createCategory(request), HttpStatus.CREATED);
    }

    // ── Get All Categories ────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns all active product categories. Public endpoint — no auth required.")
    @ApiResponse(responseCode = "200", description = "List of categories returned")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // ── Get By ID ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns a single category by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // ── Update Category (Admin) ───────────────────────────────────────────────
    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    @Operation(summary = "Update a category", description = "Updates category name/description. 🔒 Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // ── Delete Category (Admin) ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    @Operation(summary = "Delete a category", description = "Deletes a category. ⚠️ Will fail if products are linked to it. 🔒 Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category has linked products — cannot delete")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ── Get Products By Category ──────────────────────────────────────────────
    @GetMapping("/{id}/products")
    @Operation(
            summary = "Get all products in a category",
            description = "Returns a paginated list of ACTIVE products belonging to the specified category."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @Parameter(description = "Category UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.getProductsByCategory(id, pageable));
    }
}

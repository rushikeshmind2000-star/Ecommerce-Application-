package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.ApiResponse;
import com.ecommerce.inventoryservice.dto.ConfirmStockRequest;
import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.ReserveStockRequest;
import com.ecommerce.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "📦 Inventory", description = "Manage product stock levels — create, reserve, release and confirm stock")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ── Create Stock ──────────────────────────────────────────────────────────
    @PostMapping
    @Operation(
            summary = "Create stock entry for a product",
            description = "Initialises the inventory record for a product. Usually called when a vendor product is approved. 🔒 Vendor / Admin."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Stock created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Stock entry already exists for this product")
    })
    public ResponseEntity<ApiResponse<Void>> createStock(@Valid @RequestBody InventoryRequest request) {
        inventoryService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Stock created successfully", null));
    }

    // ── Get Inventory ─────────────────────────────────────────────────────────
    @GetMapping("/{productId}")
    @Operation(
            summary = "Get inventory for a product",
            description = "Returns current stock levels (available, reserved, total) for the given product UUID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventory details returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No inventory record for this product")
    })
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID productId) {
        InventoryResponse response = inventoryService.getInventory(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory fetched successfully", response));
    }

    // ── Reserve Stock ─────────────────────────────────────────────────────────
    @PostMapping("/reserve")
    @Operation(
            summary = "Reserve stock for an order",
            description = "Temporarily holds stock when a customer places an order (Saga step). Stock moves from **available → reserved**."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock reserved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient stock or validation error")
    })
    public ResponseEntity<ApiResponse<Void>> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        inventoryService.reserveStock(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock reserved successfully", null));
    }

    // ── Release Stock ─────────────────────────────────────────────────────────
    @PostMapping("/release")
    @Operation(
            summary = "Release reserved stock",
            description = "Releases previously reserved stock back to available (Saga compensation step — e.g. payment failed or order cancelled)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock released"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<Void>> releaseStock(@Valid @RequestBody ReleaseStockRequest request) {
        inventoryService.releaseStock(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock released successfully", null));
    }

    // ── Confirm Stock ─────────────────────────────────────────────────────────
    @PostMapping("/confirm")
    @Operation(
            summary = "Confirm (deduct) reserved stock",
            description = "Permanently deducts reserved stock after a successful payment (Saga commit step). Stock moves from **reserved → sold**."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock confirmed and deducted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<ApiResponse<Void>> confirmStock(@Valid @RequestBody ConfirmStockRequest request) {
        inventoryService.confirmStock(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock confirmed successfully", null));
    }
}
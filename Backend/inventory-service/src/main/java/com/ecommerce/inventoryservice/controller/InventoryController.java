package com.ecommerce.inventoryservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.inventoryservice.dto.ApiResponse;
import com.ecommerce.inventoryservice.dto.ConfirmStockRequest;
import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.ReserveStockRequest;
import com.ecommerce.inventoryservice.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private  final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStock(
            @Valid @RequestBody InventoryRequest request) {

        inventoryService.createStock(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Stock created successfully",
                        null));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @PathVariable UUID productId) {

        InventoryResponse response =
                inventoryService.getInventory(productId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Inventory fetched successfully",
                        response));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<Void>> reserveStock(
            @Valid @RequestBody ReserveStockRequest request) {

        inventoryService.reserveStock(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Stock reserved successfully",
                        null));
    }

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<Void>> releaseStock(
            @Valid @RequestBody ReleaseStockRequest request) {

        inventoryService.releaseStock(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Stock released successfully",
                        null));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmStock(
            @Valid @RequestBody ConfirmStockRequest request) {

        inventoryService.confirmStock(request);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Stock confirmed successfully",
                        null));
    }
}
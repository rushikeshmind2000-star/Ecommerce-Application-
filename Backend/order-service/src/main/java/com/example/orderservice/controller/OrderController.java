package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.UpdateOrderStatusRequest;
import com.example.orderservice.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for order management.
 * Base path: {@code /api/orders}
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management APIs — create, view, update status, cancel")
public class OrderController {

    private final OrderService orderService;

    // ------------------------------------------------------------------
    // POST /api/orders — Create a new order
    // ------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Create a new order",
               description = "Places a new order in PENDING status with the supplied items and addresses.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderDTO created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ------------------------------------------------------------------
    // GET /api/orders/{id} — Get order by UUID
    // ------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "Order UUID") @PathVariable UUID id) {

        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ------------------------------------------------------------------
    // GET /api/orders — Get all orders (admin)
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "Get all orders",
               description = "Returns every order in the system. Intended for admin use.")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // ------------------------------------------------------------------
    // GET /api/orders/user/{userId} — Order history per user
    // ------------------------------------------------------------------

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get order history for a user",
               description = "Returns all orders placed by the specified user, newest first.")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(
            @Parameter(description = "Customer UUID") @PathVariable UUID userId) {

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    // ------------------------------------------------------------------
    // PATCH /api/orders/{id}/status — Update order status
    // ------------------------------------------------------------------

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status",
               description = "Transitions an order to the requested status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "400", description = "Invalid status value"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @Parameter(description = "Order UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    // ------------------------------------------------------------------
    // POST /api/orders/{id}/cancel — Cancel an order
    // ------------------------------------------------------------------

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order",
               description = "Cancels an order unless it is already DELIVERED or CANCELLED.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order cancelled"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be cancelled")
    })
    public ResponseEntity<OrderDTO> cancelOrder(
            @Parameter(description = "Order UUID") @PathVariable UUID id) {

        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // ------------------------------------------------------------------
    // DELETE /api/orders/{id} — Hard delete an order
    // ------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order",
               description = "Permanently removes the order and all its items.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Order deleted"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order UUID") @PathVariable UUID id) {

        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}

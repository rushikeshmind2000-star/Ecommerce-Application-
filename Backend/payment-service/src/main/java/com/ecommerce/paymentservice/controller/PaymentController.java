package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.PaymentInitiateRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.dto.RefundRequest;
import com.ecommerce.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "💳 Payments", description = "Payment processing — initiate payments and process refunds")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── Initiate Payment ──────────────────────────────────────────────────────
    @PostMapping
    @Operation(
            summary = "Initiate a payment",
            description = "Creates a new payment record for an order. This is triggered by the Order Saga after stock is successfully reserved.\n\n" +
                    "⚠️ **Do NOT retry** this endpoint on failure — duplicate payments risk double-charging the customer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — check orderId and amount"),
            @ApiResponse(responseCode = "409", description = "Payment already exists for this order")
    })
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Get Payment By ID ─────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(
            summary = "Get payment details",
            description = "Returns full payment details including status (PENDING, SUCCESS, FAILED, REFUNDED) for the given payment UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment details returned"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // ── Get Payment By Order ID ───────────────────────────────────────────────
    @GetMapping("/order/{orderId}")
    @Operation(
            summary = "Get payment details by Order ID",
            description = "Returns full payment details for the given Order UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment details returned"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "Order UUID", required = true) @PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // ── Refund ────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/refund")
    @Operation(
            summary = "Process a refund",
            description = "Refunds a previously successful payment. Only payments in SUCCESS status can be refunded. 🔒 Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
            @ApiResponse(responseCode = "400", description = "Refund not allowed — payment is not in SUCCESS status"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "Payment UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.processRefund(id, request));
    }
}
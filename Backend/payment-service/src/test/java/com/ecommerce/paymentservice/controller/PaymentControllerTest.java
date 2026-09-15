package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.domain.PaymentStatus;
import com.ecommerce.paymentservice.dto.PaymentInitiateRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.dto.RefundRequest;
import com.ecommerce.paymentservice.exception.DuplicatePaymentException;
import com.ecommerce.paymentservice.exception.GlobalExceptionHandler;
import com.ecommerce.paymentservice.exception.PaymentNotFoundException;
import com.ecommerce.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /api/payments: Should return 201 when payload is valid")
    void initiatePayment_ValidPayload_ReturnsCreated() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PaymentInitiateRequest request = new PaymentInitiateRequest(orderId, userId, new BigDecimal("1200.00"), "INR", "UPI");

        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(), orderId, userId, "PAY-123",
                request.amount(), request.currency(), request.paymentMethod(),
                PaymentStatus.SUCCESS, LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.initiatePayment(any(PaymentInitiateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/payments: Should return 400 when mandatory fields are missing")
    void initiatePayment_InvalidPayload_ReturnsBadRequest() throws Exception {
        PaymentInitiateRequest invalidRequest = new PaymentInitiateRequest(null, null, new BigDecimal("-10.00"), "", "");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/payments: Should return 409 when orderId already processed")
    void initiatePayment_DuplicateOrder_ReturnsConflict() throws Exception {
        PaymentInitiateRequest request = new PaymentInitiateRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"), "INR", "UPI");

        when(paymentService.initiatePayment(any())).thenThrow(new DuplicatePaymentException("Payment already initiated"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/payments/{id}: Should return 200 when payment exists")
    void getPaymentById_Found_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse(
                id, UUID.randomUUID(), UUID.randomUUID(), "PAY-999",
                new BigDecimal("500.00"), "INR", "CARD",
                PaymentStatus.SUCCESS, LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.getPaymentById(id)).thenReturn(response);

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/payments/{id}: Should return 404 when payment does not exist")
    void getPaymentById_NotFound_ReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(paymentService.getPaymentById(id)).thenThrow(new PaymentNotFoundException("Payment not found with id: " + id));

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/payments/{id}/refund: Should return 200 when refund succeeds")
    void refundPayment_Success_ReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        RefundRequest request = new RefundRequest("Damaged item");
        PaymentResponse response = new PaymentResponse(
                id, UUID.randomUUID(), UUID.randomUUID(), "PAY-REF",
                new BigDecimal("800.00"), "INR", "CARD",
                PaymentStatus.REFUNDED, LocalDateTime.now(), LocalDateTime.now()
        );

        when(paymentService.processRefund(eq(id), any(RefundRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments/{id}/refund", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("POST /api/payments/{id}/refund: Should return 400 when reason is blank")
    void refundPayment_BlankReason_ReturnsBadRequest() throws Exception {
        UUID id = UUID.randomUUID();
        RefundRequest invalid = new RefundRequest("");

        mockMvc.perform(post("/api/payments/{id}/refund", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
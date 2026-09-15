package com.ecommerce.paymentservice.service;


import com.ecommerce.paymentservice.domain.Payment;
import com.ecommerce.paymentservice.domain.PaymentStatus;
import com.ecommerce.paymentservice.domain.PaymentTransaction;
import com.ecommerce.paymentservice.dto.PaymentInitiateRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.dto.RefundRequest;
import com.ecommerce.paymentservice.exception.DuplicatePaymentException;
import com.ecommerce.paymentservice.exception.PaymentNotFoundException;
import com.ecommerce.paymentservice.gateway.GatewayTransactionResult;
import com.ecommerce.paymentservice.gateway.MockPaymentGateway;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private MockPaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID userId;
    private PaymentInitiateRequest request;
    private Payment payment;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        request = new PaymentInitiateRequest(orderId, userId, new BigDecimal("1500.00"), "INR", "CREDIT_CARD");

        payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setPaymentReference("PAY-REF-12345");
        payment.setAmount(new BigDecimal("1500.00"));
        payment.setCurrency("INR");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.INITIATED);
    }

    @Test
    @DisplayName("initiatePayment: Should successfully create and charge payment")
    void initiatePayment_Success() {
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.processCharge(request.amount(), request.currency()))
                .thenReturn(new GatewayTransactionResult(true, "GW-TXN-123", PaymentStatus.SUCCESS, null));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.currency()).isEqualTo("INR");
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    @DisplayName("initiatePayment: Should throw DuplicatePaymentException if orderId already exists")
    void initiatePayment_DuplicateOrderId_ThrowsException() {
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.initiatePayment(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("Payment already initiated");

        verify(paymentRepository, never()).save(any(Payment.class));
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("initiatePayment: Should handle gateway decline and record FAILED status")
    void initiatePayment_GatewayDecline_RecordsFailure() {
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.processCharge(request.amount(), request.currency()))
                .thenReturn(new GatewayTransactionResult(false, null, PaymentStatus.FAILED, "INSUFFICIENT_FUNDS"));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    @DisplayName("getPaymentById: Should return payment when ID exists")
    void getPaymentById_Success() {
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(payment.getId());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("getPaymentById: Should throw PaymentNotFoundException when ID does not exist")
    void getPaymentById_NotFound_ThrowsException() {
        UUID unknownId = UUID.randomUUID();
        when(paymentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(unknownId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment not found with id");
    }

    @Test
    @DisplayName("processRefund: Should refund a payment in SUCCESS status")
    void processRefund_Success() {
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentGateway.processRefund(anyString(), eq(payment.getAmount())))
                .thenReturn(new GatewayTransactionResult(true, "GW-REF-999", PaymentStatus.REFUNDED, null));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processRefund(payment.getId(), new RefundRequest("Customer cancelled order"));

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("processRefund: Should throw IllegalStateException if payment status is not SUCCESS")
    void processRefund_NonSuccessStatus_ThrowsException() {
        payment.setStatus(PaymentStatus.INITIATED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.processRefund(payment.getId(), new RefundRequest("Return item")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only SUCCESS payments can be refunded");

        verify(paymentRepository, never()).save(payment);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("processRefund: Should throw PaymentNotFoundException if payment does not exist")
    void processRefund_NotFound_ThrowsException() {
        UUID unknownId = UUID.randomUUID();
        when(paymentRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processRefund(unknownId, new RefundRequest("Return item")))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
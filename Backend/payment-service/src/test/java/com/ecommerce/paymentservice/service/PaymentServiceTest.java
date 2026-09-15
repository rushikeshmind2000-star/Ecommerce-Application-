package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.domain.Payment;
import com.ecommerce.paymentservice.domain.PaymentStatus;
import com.ecommerce.paymentservice.dto.PaymentInitiateRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.dto.RefundRequest;
import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
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
import org.mockito.ArgumentCaptor;
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

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                transactionRepository,
                paymentGateway,
                paymentEventPublisher
        );
    }

    @Test
    @DisplayName("Should successfully initiate payment and publish Kafka event when gateway succeeds")
    void shouldInitiatePaymentAndPublishKafkaEvent() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(999.00);

        PaymentInitiateRequest request = new PaymentInitiateRequest(
                orderId,
                userId,
                amount,
                "INR",
                "UPI"
        );

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.processCharge(amount, "INR"))
                .thenReturn(new GatewayTransactionResult(true, "GW-TXN-12345", PaymentStatus.SUCCESS, null));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.amount()).isEqualTo(amount);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);

        // Verify Kafka event was dispatched with expected values
        ArgumentCaptor<PaymentCompletedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(paymentEventPublisher, times(1)).publishPaymentCompleted(eventCaptor.capture());

        PaymentCompletedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.orderId()).isEqualTo(orderId);
        assertThat(publishedEvent.userId()).isEqualTo(userId);
        assertThat(publishedEvent.amount()).isEqualTo(amount);
        assertThat(publishedEvent.status()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Should not publish Kafka event when gateway payment fails")
    void shouldNotPublishKafkaEventWhenPaymentFails() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(99999.00);

        PaymentInitiateRequest request = new PaymentInitiateRequest(
                orderId,
                userId,
                amount,
                "INR",
                "CREDIT_CARD"
        );

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.processCharge(amount, "INR"))
                .thenReturn(new GatewayTransactionResult(false, "GW-TXN-99999", PaymentStatus.FAILED, "Declined by bank"));
        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        // Verify Kafka event was NEVER sent
        verify(paymentEventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Should throw DuplicatePaymentException when payment for orderId already exists")
    void shouldThrowExceptionWhenDuplicateOrder() {
        UUID orderId = UUID.randomUUID();
        PaymentInitiateRequest request = new PaymentInitiateRequest(
                orderId,
                UUID.randomUUID(),
                BigDecimal.valueOf(100.00),
                "INR",
                "UPI"
        );

        when(paymentRepository.existsByOrderId(orderId)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.initiatePayment(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("Payment already initiated for orderId: " + orderId);

        verify(paymentRepository, never()).save(any());
        verify(paymentGateway, never()).processCharge(any(), any());
        verify(paymentEventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Should process refund successfully when payment status is SUCCESS")
    void shouldProcessRefundSuccessfully() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrderId(UUID.randomUUID());
        payment.setUserId(UUID.randomUUID());
        payment.setAmount(BigDecimal.valueOf(500.00));
        payment.setCurrency("INR");
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentGateway.processRefund(anyString(), any(BigDecimal.class)))
                .thenReturn(new GatewayTransactionResult(true, "GW-REF-001", PaymentStatus.REFUNDED, null));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundRequest refundRequest = new RefundRequest("Customer requested refund");
        PaymentResponse response = paymentService.processRefund(paymentId, refundRequest);

        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when refunding non-SUCCESS payment")
    void shouldThrowExceptionWhenRefundingNonSuccessPayment() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(PaymentStatus.INITIATED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        RefundRequest refundRequest = new RefundRequest("Cancel order");

        assertThatThrownBy(() -> paymentService.processRefund(paymentId, refundRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only SUCCESS payments can be refunded. Current status: INITIATED");

        verify(paymentGateway, never()).processRefund(anyString(), any());
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when fetching non-existent ID")
    void shouldThrowExceptionWhenPaymentNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(nonExistentId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("Payment not found with id: " + nonExistentId);
    }
}
package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.domain.Payment;
import com.ecommerce.paymentservice.domain.PaymentStatus;
import com.ecommerce.paymentservice.domain.PaymentTransaction;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentserviceRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final MockPaymentGateway paymentserviceGateway;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository paymentserviceRepository,
                          PaymentTransactionRepository transactionRepository,
                          MockPaymentGateway paymentserviceGateway,
                          PaymentEventPublisher paymentEventPublisher) {
        this.paymentserviceRepository = paymentserviceRepository;
        this.transactionRepository = transactionRepository;
        this.paymentserviceGateway = paymentserviceGateway;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        // Idempotency check on order_id
        if (paymentserviceRepository.existsByOrderId(request.orderId())) {
            throw new DuplicatePaymentException("Payment already initiated for orderId: " + request.orderId());
        }

        // 1. Create Payment record in INITIATED status
        Payment paymentservice = new Payment();
        paymentservice.setOrderId(request.orderId());
        paymentservice.setUserId(request.userId());
        paymentservice.setPaymentReference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        paymentservice.setAmount(request.amount());
        paymentservice.setCurrency(request.currency().toUpperCase());
        paymentservice.setPaymentMethod(request.paymentMethod());
        paymentservice.setStatus(PaymentStatus.INITIATED);

        Payment savedPayment = paymentserviceRepository.save(paymentservice);

        // 2. Invoke Mock Gateway
        GatewayTransactionResult result = paymentserviceGateway.processCharge(request.amount(), request.currency());

        // 3. Record PaymentTransaction attempt
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionReference("TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        transaction.setGateway("MOCK_GATEWAY");
        transaction.setGatewayTransactionId(result.gatewayTransactionId());
        transaction.setAmount(request.amount());
        transaction.setStatus(result.status());
        transaction.setFailureReason(result.failureReason());

        savedPayment.addTransaction(transaction);
        savedPayment.setStatus(result.status());

        Payment finalPayment = paymentserviceRepository.save(savedPayment);
        log.info("Payment reference {} processed with status: {}", finalPayment.getPaymentReference(), finalPayment.getStatus());

        // 4. Publish Kafka Event if payment succeeded
        if (finalPayment.getStatus() == PaymentStatus.SUCCESS) {
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    finalPayment.getId(),
                    finalPayment.getOrderId(),
                    finalPayment.getUserId(),
                    "customer@example.com", // Default placeholder until added to PaymentInitiateRequest
                    finalPayment.getAmount(),
                    finalPayment.getCurrency(),
                    finalPayment.getStatus().name()
            );
            paymentEventPublisher.publishPaymentCompleted(event);
        }

        return mapToResponse(finalPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        return paymentserviceRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }

    @Transactional
    public PaymentResponse processRefund(UUID id, RefundRequest refundRequest) {
        Payment payment = paymentserviceRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only SUCCESS payments can be refunded. Current status: " + payment.getStatus());
        }

        // Call Gateway for Refund
        GatewayTransactionResult result = paymentserviceGateway.processRefund("ORIGINAL-GW-ID", payment.getAmount());

        PaymentTransaction refundTxn = new PaymentTransaction();
        refundTxn.setTransactionReference("TXN-REFUND-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        refundTxn.setGateway("MOCK_GATEWAY");
        refundTxn.setGatewayTransactionId(result.gatewayTransactionId());
        refundTxn.setAmount(payment.getAmount());
        refundTxn.setStatus(PaymentStatus.REFUNDED);
        refundTxn.setFailureReason("Reason: " + refundRequest.reason());

        payment.addTransaction(refundTxn);
        payment.setStatus(PaymentStatus.REFUNDED);

        Payment updated = paymentserviceRepository.save(payment);
        return mapToResponse(updated);
    }

    private PaymentResponse mapToResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrderId(),
                p.getUserId(),
                p.getPaymentReference(),
                p.getAmount(),
                p.getCurrency(),
                p.getPaymentMethod(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
package com.ecommerce.paymentservice.repository;



import com.ecommerce.paymentservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByPaymentReference(String paymentReference);
    boolean existsByOrderId(UUID orderId);
}
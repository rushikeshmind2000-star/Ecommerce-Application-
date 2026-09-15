package com.ecommerce.paymentservice.repository;



import com.ecommerce.paymentservice.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    List<PaymentTransaction> findByPaymentId(UUID paymentId);
    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);
}

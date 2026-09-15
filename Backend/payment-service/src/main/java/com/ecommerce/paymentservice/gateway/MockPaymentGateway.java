package com.ecommerce.paymentservice.gateway;

import com.ecommerce.paymentservice.domain.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    public GatewayTransactionResult processCharge(BigDecimal amount, String currency) {
        log.info("Simulating gateway charge for amount: {} {}", amount, currency);

        // Deterministic simulation: amounts equal to 99999.00 simulate gateway decline
        if (amount.compareTo(new BigDecimal("99999.00")) == 0) {
            return new GatewayTransactionResult(
                    false,
                    null,
                    PaymentStatus.FAILED,
                    "INSUFFICIENT_FUNDS_OR_CARD_DECLINED"
            );
        }

        return new GatewayTransactionResult(
                true,
                "GW-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                PaymentStatus.SUCCESS,
                null
        );
    }

    public GatewayTransactionResult processRefund(String originalGatewayTxnId, BigDecimal amount) {
        log.info("Simulating refund for gateway txn: {}, amount: {}", originalGatewayTxnId, amount);
        return new GatewayTransactionResult(
                true,
                "GW-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                PaymentStatus.REFUNDED,
                null
        );
    }
}
package com.ecommerce.paymentservice.gatway;

import com.ecommerce.paymentservice.domain.PaymentStatus;
import com.ecommerce.paymentservice.gateway.GatewayTransactionResult;
import com.ecommerce.paymentservice.gateway.MockPaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentGatewayTest {

    private MockPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new MockPaymentGateway();
    }

    @Test
    @DisplayName("processCharge: Standard amount returns SUCCESS with a generated transaction ID")
    void processCharge_Success() {
        GatewayTransactionResult result = gateway.processCharge(new BigDecimal("500.00"), "INR");

        assertThat(result.successful()).isTrue();
        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.gatewayTransactionId()).startsWith("GW-TXN-");
        assertThat(result.failureReason()).isNull();
    }

    @Test
    @DisplayName("processCharge: Amount 99999.00 triggers deterministic FAILED flow")
    void processCharge_Decline() {
        GatewayTransactionResult result = gateway.processCharge(new BigDecimal("99999.00"), "INR");

        assertThat(result.successful()).isFalse();
        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.gatewayTransactionId()).isNull();
        assertThat(result.failureReason()).isEqualTo("INSUFFICIENT_FUNDS_OR_CARD_DECLINED");
    }

    @Test
    @DisplayName("processRefund: Successfully issues a refund with generated refund ID")
    void processRefund_Success() {
        GatewayTransactionResult result = gateway.processRefund("ORIGINAL-ID", new BigDecimal("500.00"));

        assertThat(result.successful()).isTrue();
        assertThat(result.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.gatewayTransactionId()).startsWith("GW-REF-");
        assertThat(result.failureReason()).isNull();
    }
}

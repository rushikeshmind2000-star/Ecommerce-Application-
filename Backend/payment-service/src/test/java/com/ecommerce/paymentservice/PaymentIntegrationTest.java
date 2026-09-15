package com.ecommerce.paymentservice;



import com.ecommerce.paymentservice.domain.PaymentStatus;
import com.ecommerce.paymentservice.dto.PaymentInitiateRequest;
import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.dto.RefundRequest;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaymentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E Flow: Initiate payment -> Fetch payment -> Refund payment")
    void completePaymentLifecycle() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PaymentInitiateRequest initiateReq = new PaymentInitiateRequest(
                orderId, userId, new BigDecimal("2500.00"), "INR", "CREDIT_CARD"
        );

        // 1. Initiate Payment
        ResponseEntity<PaymentResponse> initRes = restTemplate.postForEntity(
                "/api/payments", initiateReq, PaymentResponse.class
        );
        assertThat(initRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PaymentResponse created = initRes.getBody();
        assertThat(created).isNotNull();
        assertThat(created.status()).isEqualTo(PaymentStatus.SUCCESS);
        UUID paymentId = created.id();

        // 2. Fetch Payment by ID
        ResponseEntity<PaymentResponse> getRes = restTemplate.getForEntity(
                "/api/payments/" + paymentId, PaymentResponse.class
        );
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRes.getBody()).isNotNull();
        assertThat(getRes.getBody().paymentReference()).isEqualTo(created.paymentReference());

        // 3. Process Refund
        RefundRequest refundReq = new RefundRequest("Customer requested return");
        ResponseEntity<PaymentResponse> refundRes = restTemplate.postForEntity(
                "/api/payments/" + paymentId + "/refund", refundReq, PaymentResponse.class
        );
        assertThat(refundRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refundRes.getBody()).isNotNull();
        assertThat(refundRes.getBody().status()).isEqualTo(PaymentStatus.REFUNDED);
    }
}

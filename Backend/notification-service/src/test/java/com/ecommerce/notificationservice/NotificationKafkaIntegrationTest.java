package com.ecommerce.notificationservice;

import com.ecommerce.notificationservice.domain.Notification;
import com.ecommerce.notificationservice.domain.NotificationStatus;
import com.ecommerce.notificationservice.event.PaymentCompletedEvent;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = { "payment-events", "order-events" }
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class NotificationKafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private NotificationRepository repository;

    private KafkaTemplate<String, PaymentCompletedEvent> testKafkaTemplate;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Ensure type header is attached so the consumer knows to instantiate PaymentCompletedEvent
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        DefaultKafkaProducerFactory<String, PaymentCompletedEvent> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps);
        testKafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    @DisplayName("Should consume payment-events from embedded Kafka and write to database")
    void shouldConsumeAndSavePaymentNotification() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                paymentId,
                orderId,
                userId,
                "customer.kafka@example.com",
                BigDecimal.valueOf(499.00),
                "INR",
                "SUCCESS"
        );

        testKafkaTemplate.send("payment-events", orderId.toString(), event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Notification> records = repository.findByOrderId(orderId);
            assertThat(records).isNotEmpty();
            Notification notification = records.get(0);
            assertThat(notification.getRecipient()).isEqualTo("customer.kafka@example.com");
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notification.getContent()).contains("499");
        });
    }
}
package com.ecommerce.notificationservice.consumer;

import com.ecommerce.notificationservice.domain.Notification;
import com.ecommerce.notificationservice.domain.NotificationChannel;
import com.ecommerce.notificationservice.domain.NotificationStatus;
import com.ecommerce.notificationservice.event.OrderPlacedEvent;
import com.ecommerce.notificationservice.event.PaymentCompletedEvent;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaListenerTest {

    @Mock
    private NotificationRepository repository;

    private NotificationKafkaListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationKafkaListener(repository);
    }

    @Test
    @DisplayName("Should parse PaymentCompletedEvent and persist SENT email notification")
    void shouldHandlePaymentCompletedEvent() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentCompletedEvent event = new PaymentCompletedEvent(
                paymentId,
                orderId,
                userId,
                "buyer@example.com",
                BigDecimal.valueOf(1499.00),
                "INR",
                "SUCCESS"
        );

        listener.handlePaymentCompleted(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getRecipient()).isEqualTo("buyer@example.com");
        assertThat(saved.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getSubject()).contains(orderId.toString());
        assertThat(saved.getContent()).contains("1499.0");
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Should parse OrderPlacedEvent and persist order confirmation notification")
    void shouldHandleOrderPlacedEvent() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OrderPlacedEvent event = new OrderPlacedEvent(
                orderId,
                userId,
                "buyer@example.com",
                BigDecimal.valueOf(2500.50)
        );

        listener.handleOrderPlaced(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getRecipient()).isEqualTo("buyer@example.com");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getSubject()).contains("Order Confirmed");
        assertThat(saved.getContent()).contains("2500.5");
        assertThat(saved.getSentAt()).isNotNull();
    }
}
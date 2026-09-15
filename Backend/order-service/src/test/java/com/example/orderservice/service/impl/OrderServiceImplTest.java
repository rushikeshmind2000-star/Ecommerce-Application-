package com.example.orderservice.service.impl;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.UpdateOrderStatusRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.exception.OrderCancellationException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderServiceImpl}.
 *
 * All tests are pure unit tests — no Spring context is loaded.
 * The repository is mocked with Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ------------------------------------------------------------------
    // Shared test fixtures
    // ------------------------------------------------------------------

    private UUID orderId;
    private UUID userId;
    private Order sampleOrder;
    private OrderItem sampleItem;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId  = UUID.randomUUID();

        sampleItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Laptop Pro X")
                .sku("LAP-001")
                .quantity(2)
                .unitPrice(new BigDecimal("50000.00"))
                .totalPrice(new BigDecimal("100000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        sampleOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .orderNumber("ORD-20260914114534-ABC123")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100000.00"))
                .currency("INR")
                .shippingAddress("123 Ship Street, Mumbai")
                .billingAddress("456 Bill Avenue, Delhi")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(new ArrayList<>(List.of(sampleItem)))
                .build();

        // Wire back-reference so toItemDTO doesn't NPE
        sampleItem.setOrder(sampleOrder);
    }

    // ------------------------------------------------------------------
    // createOrder
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("createOrder()")
    class CreateOrderTests {

        @Test
        @DisplayName("should create order with PENDING status and computed total")
        void createOrder_success() {
            // Arrange
            CreateOrderRequest request = buildCreateRequest("USD");
            when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

            // Act
            OrderDTO result = orderService.createOrder(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getCurrency()).isEqualTo("INR");
            assertThat(result.getOrderNumber()).startsWith("ORD-");

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("should default currency to INR when not provided")
        void createOrder_defaultCurrency() {
            // Arrange — currency is null in request
            CreateOrderRequest request = buildCreateRequest(null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenReturn(sampleOrder);

            // Act
            orderService.createOrder(request);

            // Assert
            assertThat(captor.getValue().getCurrency()).isEqualTo("INR");
        }

        @Test
        @DisplayName("should uppercase provided currency code")
        void createOrder_uppercasesCurrency() {
            CreateOrderRequest request = buildCreateRequest("usd");

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenReturn(sampleOrder);

            orderService.createOrder(request);

            assertThat(captor.getValue().getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("should compute totalAmount as sum of (unitPrice × quantity) per item")
        void createOrder_computesTotalCorrectly() {
            CreateOrderRequest request = buildCreateRequest("INR");

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenReturn(sampleOrder);

            orderService.createOrder(request);

            // 1 item: unitPrice=50000, qty=2 → 100000
            assertThat(captor.getValue().getTotalAmount())
                    .isEqualByComparingTo(new BigDecimal("100000.00"));
        }

        @Test
        @DisplayName("should generate a unique order number prefixed with ORD-")
        void createOrder_generatesOrderNumber() {
            CreateOrderRequest request = buildCreateRequest("INR");

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(captor.capture())).thenReturn(sampleOrder);

            orderService.createOrder(request);

            String orderNumber = captor.getValue().getOrderNumber();
            assertThat(orderNumber).matches("ORD-\\d{14}-[A-F0-9]{6}");
        }
    }

    // ------------------------------------------------------------------
    // getOrderById
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderByIdTests {

        @Test
        @DisplayName("should return OrderDTO when order exists")
        void getOrderById_found() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(sampleOrder));

            OrderDTO result = orderService.getOrderById(orderId);

            assertThat(result.getId()).isEqualTo(orderId);
            assertThat(result.getOrderNumber()).isEqualTo(sampleOrder.getOrderNumber());
            assertThat(result.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("should throw OrderNotFoundException when order does not exist")
        void getOrderById_notFound() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(orderId))
                    .isInstanceOf(OrderNotFoundException.class)
                    .hasMessageContaining(orderId.toString());
        }
    }

    // ------------------------------------------------------------------
    // getAllOrders
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getAllOrders()")
    class GetAllOrdersTests {

        @Test
        @DisplayName("should return all orders as DTOs")
        void getAllOrders_returnsList() {
            when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

            List<OrderDTO> result = orderService.getAllOrders();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("should return empty list when no orders exist")
        void getAllOrders_empty() {
            when(orderRepository.findAll()).thenReturn(List.of());

            assertThat(orderService.getAllOrders()).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // getOrdersByUserId
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getOrdersByUserId()")
    class GetOrdersByUserIdTests {

        @Test
        @DisplayName("should return orders for a specific user")
        void getOrdersByUserId_found() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of(sampleOrder));

            List<OrderDTO> result = orderService.getOrdersByUserId(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should return empty list when user has no orders")
        void getOrdersByUserId_empty() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .thenReturn(List.of());

            assertThat(orderService.getOrdersByUserId(userId)).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // updateOrderStatus
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("should update status and return updated DTO")
        void updateStatus_success() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(sampleOrder));

            Order updatedOrder = cloneWithStatus(sampleOrder, OrderStatus.CONFIRMED);
            when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

            UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

            OrderDTO result = orderService.updateOrderStatus(orderId, req);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            verify(orderRepository).save(sampleOrder);
        }

        @Test
        @DisplayName("should throw OrderNotFoundException when order not found")
        void updateStatus_notFound() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

            assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, req))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ------------------------------------------------------------------
    // cancelOrder
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrderTests {

        @Test
        @DisplayName("should cancel a PENDING order successfully")
        void cancelOrder_pending_success() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(sampleOrder));
            Order cancelled = cloneWithStatus(sampleOrder, OrderStatus.CANCELLED);
            when(orderRepository.save(any())).thenReturn(cancelled);

            OrderDTO result = orderService.cancelOrder(orderId);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @ParameterizedTest(name = "should reject cancellation when status is {0}")
        @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
        @DisplayName("should throw OrderCancellationException for terminal states")
        void cancelOrder_terminalState_throws(OrderStatus terminalStatus) {
            Order terminalOrder = cloneWithStatus(sampleOrder, terminalStatus);
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(terminalOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(OrderCancellationException.class)
                    .hasMessageContaining(terminalStatus.name());
        }

        @Test
        @DisplayName("should throw OrderNotFoundException when order does not exist")
        void cancelOrder_notFound() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ------------------------------------------------------------------
    // deleteOrder
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("deleteOrder()")
    class DeleteOrderTests {

        @Test
        @DisplayName("should delete an existing order")
        void deleteOrder_success() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(sampleOrder));

            orderService.deleteOrder(orderId);

            verify(orderRepository, times(1)).delete(sampleOrder);
        }

        @Test
        @DisplayName("should throw OrderNotFoundException when order not found")
        void deleteOrder_notFound() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                    .isInstanceOf(OrderNotFoundException.class);

            verify(orderRepository, never()).delete(any());
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private CreateOrderRequest buildCreateRequest(String currency) {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(UUID.randomUUID())
                .productName("Laptop Pro X")
                .sku("LAP-001")
                .quantity(2)
                .unitPrice(new BigDecimal("50000.00"))
                .build();

        return CreateOrderRequest.builder()
                .userId(userId)
                .shippingAddress("123 Ship Street, Mumbai")
                .billingAddress("456 Bill Avenue, Delhi")
                .currency(currency)
                .items(List.of(itemRequest))
                .build();
    }

    /** Creates a shallow clone of an Order with a different status (for return stubbing). */
    private Order cloneWithStatus(Order source, OrderStatus status) {
        return Order.builder()
                .id(source.getId())
                .userId(source.getUserId())
                .orderNumber(source.getOrderNumber())
                .status(status)
                .totalAmount(source.getTotalAmount())
                .currency(source.getCurrency())
                .shippingAddress(source.getShippingAddress())
                .billingAddress(source.getBillingAddress())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .items(source.getItems())
                .build();
    }
}

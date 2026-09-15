package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.OrderItemDTO;
import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.UpdateOrderStatusRequest;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.exception.GlobalExceptionHandler;
import com.example.orderservice.exception.OrderCancellationException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.service.OrderService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link OrderController} using {@code @WebMvcTest}.
 *
 * Only the Web layer (serialization, routing, validation) is loaded.
 * {@link OrderService} is mocked.
 * {@link GlobalExceptionHandler} is imported so error responses are also tested.
 */
@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("OrderController Web Layer Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private UUID orderId;
    private UUID userId;
    private OrderDTO sampleOrderDTO;
    private CreateOrderRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId  = UUID.randomUUID();

        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .productId(UUID.randomUUID())
                .productName("Laptop Pro X")
                .sku("LAP-001")
                .quantity(2)
                .unitPrice(new BigDecimal("50000.00"))
                .totalPrice(new BigDecimal("100000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        sampleOrderDTO = OrderDTO.builder()
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
                .items(List.of(itemDTO))
                .build();

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(UUID.randomUUID())
                .productName("Laptop Pro X")
                .sku("LAP-001")
                .quantity(2)
                .unitPrice(new BigDecimal("50000.00"))
                .build();

        validCreateRequest = CreateOrderRequest.builder()
                .userId(userId)
                .shippingAddress("123 Ship Street, Mumbai")
                .billingAddress("456 Bill Avenue, Delhi")
                .currency("INR")
                .items(List.of(itemRequest))
                .build();
    }

    // ------------------------------------------------------------------
    // POST /api/orders
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrderTests {

        @Test
        @DisplayName("should return 201 with created order")
        void createOrder_201() throws Exception {
            when(orderService.createOrder(any())).thenReturn(sampleOrderDTO);

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderNumber").value("ORD-20260914114534-ABC123"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.currency").value("INR"))
                    .andExpect(jsonPath("$.items", hasSize(1)));
        }

        @Test
        @DisplayName("should return 400 when request body is missing")
        void createOrder_missingBody_400() throws Exception {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when userId is null")
        void createOrder_missingUserId_400() throws Exception {
            CreateOrderRequest bad = CreateOrderRequest.builder()
                    .userId(null)                         // violates @NotNull
                    .shippingAddress("Ship St")
                    .billingAddress("Bill Ave")
                    .items(List.of(OrderItemRequest.builder()
                            .productId(UUID.randomUUID())
                            .productName("Widget")
                            .sku("W-001")
                            .quantity(1)
                            .unitPrice(BigDecimal.TEN)
                            .build()))
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.userId").exists());
        }

        @Test
        @DisplayName("should return 400 when items list is empty")
        void createOrder_emptyItems_400() throws Exception {
            CreateOrderRequest bad = CreateOrderRequest.builder()
                    .userId(userId)
                    .shippingAddress("Ship St")
                    .billingAddress("Bill Ave")
                    .items(List.of())                     // violates @NotEmpty
                    .build();

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.items").exists());
        }
    }

    // ------------------------------------------------------------------
    // GET /api/orders/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrderByIdTests {

        @Test
        @DisplayName("should return 200 with order when found")
        void getById_200() throws Exception {
            when(orderService.getOrderById(orderId)).thenReturn(sampleOrderDTO);

            mockMvc.perform(get("/api/orders/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId.toString()))
                    .andExpect(jsonPath("$.orderNumber").value("ORD-20260914114534-ABC123"));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void getById_404() throws Exception {
            when(orderService.getOrderById(orderId))
                    .thenThrow(new OrderNotFoundException("Order not found with id: " + orderId));

            mockMvc.perform(get("/api/orders/{id}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Order Not Found"))
                    .andExpect(jsonPath("$.message").value(containsString(orderId.toString())));
        }
    }

    // ------------------------------------------------------------------
    // GET /api/orders
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/orders")
    class GetAllOrdersTests {

        @Test
        @DisplayName("should return 200 with all orders")
        void getAll_200() throws Exception {
            when(orderService.getAllOrders()).thenReturn(List.of(sampleOrderDTO));

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].orderNumber").value("ORD-20260914114534-ABC123"));
        }

        @Test
        @DisplayName("should return 200 with empty list")
        void getAll_empty() throws Exception {
            when(orderService.getAllOrders()).thenReturn(List.of());

            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ------------------------------------------------------------------
    // GET /api/orders/user/{userId}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/orders/user/{userId}")
    class GetOrdersByUserTests {

        @Test
        @DisplayName("should return 200 with user's orders")
        void getByUser_200() throws Exception {
            when(orderService.getOrdersByUserId(userId)).thenReturn(List.of(sampleOrderDTO));

            mockMvc.perform(get("/api/orders/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].userId").value(userId.toString()));
        }

        @Test
        @DisplayName("should return 200 with empty list for user with no orders")
        void getByUser_empty() throws Exception {
            when(orderService.getOrdersByUserId(userId)).thenReturn(List.of());

            mockMvc.perform(get("/api/orders/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ------------------------------------------------------------------
    // PATCH /api/orders/{id}/status
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("PATCH /api/orders/{id}/status")
    class UpdateStatusTests {

        @Test
        @DisplayName("should return 200 with updated order")
        void updateStatus_200() throws Exception {
            OrderDTO confirmed = OrderDTO.builder()
                    .id(orderId).userId(userId)
                    .orderNumber("ORD-20260914114534-ABC123")
                    .status(OrderStatus.CONFIRMED)
                    .totalAmount(sampleOrderDTO.getTotalAmount())
                    .currency("INR")
                    .shippingAddress("Ship St").billingAddress("Bill Ave")
                    .items(List.of())
                    .build();

            when(orderService.updateOrderStatus(eq(orderId), any())).thenReturn(confirmed);

            UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);

            mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void updateStatus_404() throws Exception {
            when(orderService.updateOrderStatus(eq(orderId), any()))
                    .thenThrow(new OrderNotFoundException("Order not found with id: " + orderId));

            UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.SHIPPED);

            mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("should return 400 when status field is missing")
        void updateStatus_missingStatus_400() throws Exception {
            mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": null}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when status value is unrecognised enum string")
        void updateStatus_invalidEnum_400() throws Exception {
            mockMvc.perform(patch("/api/orders/{id}/status", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"INVALID_STATE\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Malformed Request"));
        }
    }

    // ------------------------------------------------------------------
    // POST /api/orders/{id}/cancel
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/orders/{id}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("should return 200 with cancelled order")
        void cancel_200() throws Exception {
            OrderDTO cancelled = OrderDTO.builder()
                    .id(orderId).userId(userId)
                    .orderNumber("ORD-20260914114534-ABC123")
                    .status(OrderStatus.CANCELLED)
                    .totalAmount(sampleOrderDTO.getTotalAmount())
                    .currency("INR")
                    .shippingAddress("Ship St").billingAddress("Bill Ave")
                    .items(List.of())
                    .build();

            when(orderService.cancelOrder(orderId)).thenReturn(cancelled);

            mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("should return 409 when order is already in terminal state")
        void cancel_conflict_409() throws Exception {
            when(orderService.cancelOrder(orderId))
                    .thenThrow(new OrderCancellationException(
                            "Order 'ORD-X' cannot be cancelled because it is already in 'DELIVERED' state."));

            mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Order Cannot Be Cancelled"))
                    .andExpect(jsonPath("$.message").value(containsString("DELIVERED")));
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void cancel_404() throws Exception {
            when(orderService.cancelOrder(orderId))
                    .thenThrow(new OrderNotFoundException("Order not found with id: " + orderId));

            mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                    .andExpect(status().isNotFound());
        }
    }

    // ------------------------------------------------------------------
    // DELETE /api/orders/{id}
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/orders/{id}")
    class DeleteOrderTests {

        @Test
        @DisplayName("should return 204 on successful delete")
        void delete_204() throws Exception {
            doNothing().when(orderService).deleteOrder(orderId);

            mockMvc.perform(delete("/api/orders/{id}", orderId))
                    .andExpect(status().isNoContent());

            verify(orderService, times(1)).deleteOrder(orderId);
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void delete_404() throws Exception {
            doThrow(new OrderNotFoundException("Order not found with id: " + orderId))
                    .when(orderService).deleteOrder(orderId);

            mockMvc.perform(delete("/api/orders/{id}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}

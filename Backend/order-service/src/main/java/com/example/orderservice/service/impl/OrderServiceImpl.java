package com.example.orderservice.service.impl;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.OrderItemDTO;
import com.example.orderservice.dto.UpdateOrderStatusRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.exception.OrderCancellationException;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link OrderService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** States from which an order can no longer be cancelled. */
    private static final List<OrderStatus> NON_CANCELLABLE_STATUSES =
            List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private final OrderRepository orderRepository;

    // ------------------------------------------------------------------
    // Create
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {

        log.info("Creating order for user: {}", request.getUserId());

        // Build the parent order shell
        Order order = Order.builder()
                .userId(request.getUserId())
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR")
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .totalAmount(BigDecimal.ZERO)   // recalculated below
                .build();

        // Map and attach each item
        request.getItems().forEach(itemReq -> {
            BigDecimal lineTotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .sku(itemReq.getSku())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .totalPrice(lineTotal)
                    .build();

            order.addItem(item);
        });

        // Compute grand total from items
        order.recalculateTotal();

        Order saved = orderRepository.save(order);

        log.info("Order created: {} for user: {}", saved.getOrderNumber(), saved.getUserId());

        return toDTO(saved);
    }

    // ------------------------------------------------------------------
    // Read
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID id) {

        Order order = findOrThrow(id);
        return toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(UUID userId) {

        log.info("Fetching order history for user: {}", userId);

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ------------------------------------------------------------------
    // Update status
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {

        Order order = findOrThrow(id);

        log.info("Updating order {} status: {} → {}", order.getOrderNumber(),
                order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());

        return toDTO(orderRepository.save(order));
    }

    // ------------------------------------------------------------------
    // Cancel
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public OrderDTO cancelOrder(UUID id) {

        Order order = findOrThrow(id);

        if (NON_CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new OrderCancellationException(
                    String.format(
                            "Order '%s' cannot be cancelled because it is already in '%s' state.",
                            order.getOrderNumber(), order.getStatus()
                    )
            );
        }

        log.info("Cancelling order: {}", order.getOrderNumber());

        order.setStatus(OrderStatus.CANCELLED);

        return toDTO(orderRepository.save(order));
    }

    // ------------------------------------------------------------------
    // Delete
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void deleteOrder(UUID id) {

        Order order = findOrThrow(id);

        log.info("Deleting order: {}", order.getOrderNumber());

        orderRepository.delete(order);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Fetches an order or throws {@link OrderNotFoundException}.
     */
    private Order findOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + id));
    }

    /**
     * Generates a unique, human-readable order number.
     * Format: {@code ORD-{yyyyMMddHHmmss}-{6-char uppercase hex}}
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(ORDER_NUMBER_FORMAT);
        String hex = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + hex;
    }

    /**
     * Maps an {@link Order} entity to an {@link OrderDTO}.
     */
    private OrderDTO toDTO(Order order) {

        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::toItemDTO)
                .toList();

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }

    /**
     * Maps an {@link OrderItem} entity to an {@link OrderItemDTO}.
     */
    private OrderItemDTO toItemDTO(OrderItem item) {

        return OrderItemDTO.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .createdAt(item.getCreatedAt())
                .build();
    }
}

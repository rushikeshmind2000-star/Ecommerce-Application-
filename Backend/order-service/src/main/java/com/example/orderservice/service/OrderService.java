package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.dto.UpdateOrderStatusRequest;

import java.util.List;
import java.util.UUID;

/**
 * Business operations for the order domain.
 */
public interface OrderService {

    /**
     * Creates a new order in PENDING status.
     *
     * @param request creation payload with user, addresses, and items
     * @return the persisted order as a DTO
     */
    OrderDTO createOrder(CreateOrderRequest request);

    /**
     * Retrieves a single order by its UUID primary key.
     *
     * @param id order UUID
     * @return the order DTO
     * @throws com.example.orderservice.exception.OrderNotFoundException if not found
     */
    OrderDTO getOrderById(UUID id);

    /**
     * Retrieves all orders in the system (admin use).
     *
     * @return list of all orders
     */
    List<OrderDTO> getAllOrders();

    /**
     * Retrieves the full order history for a specific user,
     * sorted by newest first.
     *
     * @param userId customer UUID
     * @return list of orders for this user
     */
    List<OrderDTO> getOrdersByUserId(UUID userId);

    /**
     * Patches the status of an existing order.
     *
     * @param id      order UUID
     * @param request new status
     * @return updated order DTO
     */
    OrderDTO updateOrderStatus(UUID id, UpdateOrderStatusRequest request);

    /**
     * Cancels an order. Only orders that are not yet DELIVERED or CANCELLED
     * may be cancelled.
     *
     * @param id order UUID
     * @return updated order DTO with status CANCELLED
     * @throws com.example.orderservice.exception.OrderCancellationException if the
     *         order is already in a terminal state
     */
    OrderDTO cancelOrder(UUID id);

    /**
     * Hard-deletes an order and all its items.
     *
     * @param id order UUID
     */
    void deleteOrder(UUID id);
}

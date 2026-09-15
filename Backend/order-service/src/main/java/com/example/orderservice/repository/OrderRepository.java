package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Order}.
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /** Returns all orders belonging to a specific user (order history). */
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Looks up a single order by its unique human-readable order number. */
    Optional<Order> findByOrderNumber(String orderNumber);

    /** Returns all orders currently in the given status. */
    List<Order> findByStatus(OrderStatus status);
}

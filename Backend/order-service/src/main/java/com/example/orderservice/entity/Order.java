package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a customer order.
 * Maps to the {@code orders} table.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The customer who placed the order. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Human-readable, unique order reference.
     * Format: ORD-{yyyyMMddHHmmss}-{6-char random hex}
     */
    @Column(name = "order_number", unique = true, nullable = false, updatable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    /** Sum of all order-item totals. */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /** ISO 4217 currency code, default INR. */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "billing_address", nullable = false)
    private String billingAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Child items — owned by this order. */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ---------------------------------------------------------------
    // Convenience helpers
    // ---------------------------------------------------------------

    /** Adds an item and wires the back-reference. */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }

    /** Recalculates totalAmount from current items. */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.douae.ems.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Order() {
    }

    public static Order place(String customerId, String sku, int quantity, BigDecimal unitPrice) {
        Order order = new Order();
        order.id = UUID.randomUUID().toString();
        order.customerId = customerId;
        order.sku = sku;
        order.quantity = quantity;
        order.unitPrice = unitPrice;
        order.status = OrderStatus.PLACED;
        order.createdAt = Instant.now();
        return order;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

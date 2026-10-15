package com.douae.ems.query.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Denormalized read model for order fulfilment status.
 * Separate DB from order-service (database-per-service).
 */
@Entity
@Table(name = "order_views")
public class OrderView {

    @Id
    @Column(length = 36)
    private String orderId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 32)
    private String fulfilmentStatus;

    private Integer remainingStock;

    private String rejectionReason;

    @Column(nullable = false)
    private Instant lastUpdatedAt;

    protected OrderView() {
    }

    public static OrderView fromPlaced(String orderId, String customerId, String sku,
                                       int quantity, BigDecimal unitPrice, Instant at) {
        OrderView view = new OrderView();
        view.orderId = orderId;
        view.customerId = customerId;
        view.sku = sku;
        view.quantity = quantity;
        view.unitPrice = unitPrice;
        view.fulfilmentStatus = "PENDING_INVENTORY";
        view.lastUpdatedAt = at;
        return view;
    }

    /** Merge placed identity onto a stub created by an out-of-order inventory event. */
    public void applyPlaced(String customerId, String sku, int quantity,
                            BigDecimal unitPrice, Instant at) {
        this.customerId = customerId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        if ("PENDING_INVENTORY".equals(this.fulfilmentStatus) || this.fulfilmentStatus == null) {
            this.fulfilmentStatus = "PENDING_INVENTORY";
        }
        this.lastUpdatedAt = at;
    }

    public void markReserved(int remainingStock, Instant at) {
        this.fulfilmentStatus = "RESERVED";
        this.remainingStock = remainingStock;
        this.rejectionReason = null;
        this.lastUpdatedAt = at;
    }

    public void markRejected(String reason, Integer availableStock, Instant at) {
        this.fulfilmentStatus = "REJECTED";
        this.rejectionReason = reason;
        this.remainingStock = availableStock;
        this.lastUpdatedAt = at;
    }

    public String getOrderId() {
        return orderId;
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

    public String getFulfilmentStatus() {
        return fulfilmentStatus;
    }

    public Integer getRemainingStock() {
        return remainingStock;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}

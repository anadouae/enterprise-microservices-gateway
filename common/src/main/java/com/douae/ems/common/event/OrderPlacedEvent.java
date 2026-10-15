package com.douae.ems.common.event;

import java.math.BigDecimal;

/**
 * Command-side fact: an order was accepted and persisted by order-service.
 * Downstream: inventory-service reserves stock; query-service projects read model.
 */
public class OrderPlacedEvent extends DomainEvent {

    private String customerId;
    private String sku;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(String orderId, String customerId, String sku, int quantity, BigDecimal unitPrice) {
        super(orderId);
        this.customerId = customerId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}

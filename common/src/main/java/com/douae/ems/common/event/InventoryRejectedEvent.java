package com.douae.ems.common.event;

/** Inventory could not fulfil the order (insufficient stock or unknown SKU). */
public class InventoryRejectedEvent extends DomainEvent {

    private String sku;
    private int requestedQuantity;
    private int availableStock;
    private String reason;

    public InventoryRejectedEvent() {
    }

    public InventoryRejectedEvent(String orderId, String sku, int requestedQuantity,
                                  int availableStock, String reason) {
        super(orderId);
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
        this.reason = reason;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

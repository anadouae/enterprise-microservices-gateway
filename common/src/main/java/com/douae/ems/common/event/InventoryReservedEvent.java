package com.douae.ems.common.event;

/** Inventory successfully reserved against an order. */
public class InventoryReservedEvent extends DomainEvent {

    private String sku;
    private int quantity;
    private int remainingStock;

    public InventoryReservedEvent() {
    }

    public InventoryReservedEvent(String orderId, String sku, int quantity, int remainingStock) {
        super(orderId);
        this.sku = sku;
        this.quantity = quantity;
        this.remainingStock = remainingStock;
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

    public int getRemainingStock() {
        return remainingStock;
    }

    public void setRemainingStock(int remainingStock) {
        this.remainingStock = remainingStock;
    }
}

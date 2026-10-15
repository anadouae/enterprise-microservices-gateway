package com.douae.ems.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @Column(length = 64)
    private String sku;

    @Column(nullable = false)
    private int availableQuantity;

    @Version
    private long version;

    protected StockItem() {
    }

    public StockItem(String sku, int availableQuantity) {
        this.sku = sku;
        this.availableQuantity = availableQuantity;
    }

    public String getSku() {
        return sku;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * Optimistic-lock guarded reservation.
     *
     * <p><b>Complexity note:</b> O(1) decrement under {@code @Version}. Concurrent
     * reservations on the same SKU retry via JPA OptimisticLockException — preferred
     * over pessimistic locks for short critical sections in this demo.
     */
    public boolean tryReserve(int quantity) {
        if (availableQuantity < quantity) {
            return false;
        }
        availableQuantity -= quantity;
        return true;
    }
}

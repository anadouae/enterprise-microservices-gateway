package com.douae.ems.inventory.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockItemTest {

    @Test
    void tryReserveDecrementsWhenEnough() {
        StockItem item = new StockItem("SKU-A", 5);
        assertThat(item.tryReserve(3)).isTrue();
        assertThat(item.getAvailableQuantity()).isEqualTo(2);
    }

    @Test
    void tryReserveFailsWhenShort() {
        StockItem item = new StockItem("SKU-A", 2);
        assertThat(item.tryReserve(3)).isFalse();
        assertThat(item.getAvailableQuantity()).isEqualTo(2);
    }
}

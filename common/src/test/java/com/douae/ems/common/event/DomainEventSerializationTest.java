package com.douae.ems.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void roundTripsOrderPlacedWithPolymorphicType() throws Exception {
        OrderPlacedEvent original = new OrderPlacedEvent(
                "ord-1", "cust-1", "SKU-A", 2, new BigDecimal("5.50"));

        String json = mapper.writeValueAsString(original);
        assertThat(json).contains("\"eventType\":\"OrderPlaced\"");

        DomainEvent decoded = mapper.readValue(json, DomainEvent.class);
        assertThat(decoded).isInstanceOf(OrderPlacedEvent.class);
        assertThat(((OrderPlacedEvent) decoded).getSku()).isEqualTo("SKU-A");
    }

    @Test
    void roundTripsInventoryReserved() throws Exception {
        InventoryReservedEvent original = new InventoryReservedEvent("ord-1", "SKU-A", 2, 8);
        DomainEvent decoded = mapper.readValue(mapper.writeValueAsString(original), DomainEvent.class);
        assertThat(decoded).isInstanceOf(InventoryReservedEvent.class);
    }
}

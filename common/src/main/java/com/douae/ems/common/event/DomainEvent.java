package com.douae.ems.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope for all domain events crossing the Kafka bus.
 *
 * <p><b>Complexity note:</b> polymorphic Jackson typing is O(1) dispatch via
 * {@code @type}; avoid deep inheritance trees — each new event type is a flat
 * subtype registered here so consumers stay schema-compatible without codegen.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderPlacedEvent.class, name = "OrderPlaced"),
        @JsonSubTypes.Type(value = InventoryReservedEvent.class, name = "InventoryReserved"),
        @JsonSubTypes.Type(value = InventoryRejectedEvent.class, name = "InventoryRejected")
})
public abstract class DomainEvent {

    private UUID eventId;
    private Instant occurredAt;
    private String aggregateId;

    protected DomainEvent() {
    }

    protected DomainEvent(String aggregateId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
}

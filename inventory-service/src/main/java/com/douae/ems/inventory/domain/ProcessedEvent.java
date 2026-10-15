package com.douae.ems.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Idempotency ledger: each inbound Kafka eventId is recorded once. */
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @Column(length = 36)
    private String eventId;

    @Column(nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(UUID eventId) {
        this.eventId = eventId.toString();
        this.processedAt = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }
}

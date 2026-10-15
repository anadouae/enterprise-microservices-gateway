package com.douae.ems.inventory.service;

import com.douae.ems.common.event.InventoryRejectedEvent;
import com.douae.ems.common.event.InventoryReservedEvent;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.inventory.domain.ProcessedEvent;
import com.douae.ems.inventory.domain.StockItem;
import com.douae.ems.inventory.messaging.InventoryEventPublisher;
import com.douae.ems.inventory.repository.ProcessedEventRepository;
import com.douae.ems.inventory.repository.StockItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies OrderPlaced events against the inventory write model.
 *
 * <p><b>Complexity note:</b> idempotency check is O(1) PK lookup on eventId.
 * Reservation is O(1) SKU lookup + optimistic version bump. Duplicate Kafka
 * deliveries short-circuit before mutating stock — critical for at-least-once.
 */
@Service
public class InventoryReservationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationService.class);

    private final StockItemRepository stockItemRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryEventPublisher eventPublisher;

    public InventoryReservationService(StockItemRepository stockItemRepository,
                                       ProcessedEventRepository processedEventRepository,
                                       InventoryEventPublisher eventPublisher) {
        this.stockItemRepository = stockItemRepository;
        this.processedEventRepository = processedEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        String eventId = event.getEventId().toString();
        if (processedEventRepository.existsById(eventId)) {
            log.info("Skipping duplicate eventId={} for order={}", eventId, event.getAggregateId());
            return;
        }

        StockItem item = stockItemRepository.findById(event.getSku()).orElse(null);
        if (item == null) {
            eventPublisher.publish(new InventoryRejectedEvent(
                    event.getAggregateId(), event.getSku(), event.getQuantity(), 0, "UNKNOWN_SKU"));
        } else if (!item.tryReserve(event.getQuantity())) {
            eventPublisher.publish(new InventoryRejectedEvent(
                    event.getAggregateId(), event.getSku(), event.getQuantity(),
                    item.getAvailableQuantity(), "INSUFFICIENT_STOCK"));
        } else {
            stockItemRepository.save(item);
            eventPublisher.publish(new InventoryReservedEvent(
                    event.getAggregateId(), event.getSku(), event.getQuantity(),
                    item.getAvailableQuantity()));
        }

        processedEventRepository.save(new ProcessedEvent(event.getEventId()));
    }
}

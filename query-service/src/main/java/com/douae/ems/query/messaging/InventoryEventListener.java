package com.douae.ems.query.messaging;

import com.douae.ems.common.Topics;
import com.douae.ems.common.event.DomainEvent;
import com.douae.ems.common.event.InventoryRejectedEvent;
import com.douae.ems.common.event.InventoryReservedEvent;
import com.douae.ems.query.service.OrderProjectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private final OrderProjectionService projectionService;

    public InventoryEventListener(OrderProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @KafkaListener(topics = Topics.INVENTORY_EVENTS, groupId = "query-service-inventory",
            containerFactory = "domainEventListenerFactory")
    public void onInventoryEvent(DomainEvent event) {
        if (event instanceof InventoryReservedEvent reserved) {
            projectionService.onInventoryReserved(reserved);
        } else if (event instanceof InventoryRejectedEvent rejected) {
            projectionService.onInventoryRejected(rejected);
        }
    }
}

package com.douae.ems.inventory.messaging;

import com.douae.ems.common.Topics;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.inventory.service.InventoryReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final InventoryReservationService reservationService;

    public OrderEventListener(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "inventory-service")
    public void onOrderEvent(OrderPlacedEvent event) {
        log.info("Received OrderPlaced order={} sku={} qty={}",
                event.getAggregateId(), event.getSku(), event.getQuantity());
        reservationService.handleOrderPlaced(event);
    }
}

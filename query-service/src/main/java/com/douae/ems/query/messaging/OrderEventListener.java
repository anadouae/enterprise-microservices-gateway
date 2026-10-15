package com.douae.ems.query.messaging;

import com.douae.ems.common.Topics;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.query.service.OrderProjectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private final OrderProjectionService projectionService;

    public OrderEventListener(OrderProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "query-service-orders",
            containerFactory = "orderPlacedListenerFactory")
    public void onOrderPlaced(OrderPlacedEvent event) {
        projectionService.onOrderPlaced(event);
    }
}

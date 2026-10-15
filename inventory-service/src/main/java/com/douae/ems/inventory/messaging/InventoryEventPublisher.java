package com.douae.ems.inventory.messaging;

import com.douae.ems.common.Topics;
import com.douae.ems.common.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(DomainEvent event) {
        kafkaTemplate.send(Topics.INVENTORY_EVENTS, event.getAggregateId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inventory event for order={}", event.getAggregateId(), ex);
                    } else {
                        log.info("Published {} for order={}",
                                event.getClass().getSimpleName(), event.getAggregateId());
                    }
                });
    }
}

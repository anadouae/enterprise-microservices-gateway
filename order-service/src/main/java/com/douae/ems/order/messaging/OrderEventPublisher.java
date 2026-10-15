package com.douae.ems.order.messaging;

import com.douae.ems.common.Topics;
import com.douae.ems.common.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter: publishes command-side facts to Kafka.
 *
 * <p><b>Complexity note:</b> partition key = orderId so all events for one aggregate
 * land on the same partition (ordered per aggregate, O(1) key hash). Send is fire-and-
 * forget with callback logging — at-least-once delivery; consumers must be idempotent.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderPlacedEvent event) {
        kafkaTemplate.send(Topics.ORDER_EVENTS, event.getAggregateId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderPlaced for order={}", event.getAggregateId(), ex);
                    } else {
                        log.info("Published OrderPlaced order={} partition={}",
                                event.getAggregateId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}

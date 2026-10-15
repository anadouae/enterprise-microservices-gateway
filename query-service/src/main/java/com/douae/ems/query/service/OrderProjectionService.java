package com.douae.ems.query.service;

import com.douae.ems.common.dto.OrderViewResponse;
import com.douae.ems.common.event.InventoryRejectedEvent;
import com.douae.ems.common.event.InventoryReservedEvent;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.query.domain.OrderView;
import com.douae.ems.query.repository.OrderViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Materializes the CQRS read model from domain events.
 *
 * <p><b>Complexity note:</b> each projector method is O(1) upsert by orderId PK.
 * Eventual consistency window = Kafka lag + TX commit; queries never hit the
 * command DB. Out-of-order inventory events before OrderPlaced create a stub
 * row that OrderPlaced later fills — tolerant merge, not strict FSM.
 */
@Service
public class OrderProjectionService {

    private static final Logger log = LoggerFactory.getLogger(OrderProjectionService.class);

    private final OrderViewRepository orderViewRepository;

    public OrderProjectionService(OrderViewRepository orderViewRepository) {
        this.orderViewRepository = orderViewRepository;
    }

    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        OrderView view = orderViewRepository.findById(event.getAggregateId())
                .orElseGet(() -> OrderView.fromPlaced(
                        event.getAggregateId(),
                        event.getCustomerId(),
                        event.getSku(),
                        event.getQuantity(),
                        event.getUnitPrice(),
                        event.getOccurredAt()));
        view.applyPlaced(
                event.getCustomerId(),
                event.getSku(),
                event.getQuantity(),
                event.getUnitPrice(),
                event.getOccurredAt());
        orderViewRepository.save(view);
        log.info("Projected OrderPlaced order={}", event.getAggregateId());
    }

    @Transactional
    public void onInventoryReserved(InventoryReservedEvent event) {
        OrderView view = orderViewRepository.findById(event.getAggregateId())
                .orElseGet(() -> stub(event.getAggregateId(), event.getSku(), event.getQuantity()));
        view.markReserved(event.getRemainingStock(), event.getOccurredAt());
        orderViewRepository.save(view);
        log.info("Projected InventoryReserved order={}", event.getAggregateId());
    }

    @Transactional
    public void onInventoryRejected(InventoryRejectedEvent event) {
        OrderView view = orderViewRepository.findById(event.getAggregateId())
                .orElseGet(() -> stub(event.getAggregateId(), event.getSku(), event.getRequestedQuantity()));
        view.markRejected(event.getReason(), event.getAvailableStock(), event.getOccurredAt());
        orderViewRepository.save(view);
        log.info("Projected InventoryRejected order={}", event.getAggregateId());
    }

    @Transactional(readOnly = true)
    public OrderViewResponse getById(String orderId) {
        return orderViewRepository.findById(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderViewNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderViewResponse> byCustomer(String customerId) {
        return orderViewRepository.findByCustomerIdOrderByLastUpdatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderView stub(String orderId, String sku, int quantity) {
        return OrderView.fromPlaced(orderId, "UNKNOWN", sku, quantity, BigDecimal.ZERO, Instant.now());
    }

    private OrderViewResponse toResponse(OrderView view) {
        return new OrderViewResponse(
                view.getOrderId(),
                view.getCustomerId(),
                view.getSku(),
                view.getQuantity(),
                view.getUnitPrice(),
                view.getFulfilmentStatus(),
                view.getRemainingStock(),
                view.getRejectionReason(),
                view.getLastUpdatedAt());
    }
}

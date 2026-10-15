package com.douae.ems.order.service;

import com.douae.ems.common.dto.OrderResponse;
import com.douae.ems.common.dto.PlaceOrderRequest;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.order.domain.Order;
import com.douae.ems.order.messaging.OrderEventPublisher;
import com.douae.ems.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command handler for placing orders (write model).
 *
 * <p><b>Complexity note:</b> persist-then-publish is O(1) DB + async Kafka send.
 * Dual-write risk (DB commit, Kafka fail) is accepted for this demo; production would
 * use an outbox table scanned by a relay (same TX as Order insert).
 */
@Service
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderCommandService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Order order = Order.place(
                request.customerId(),
                request.sku(),
                request.quantity(),
                request.unitPrice());
        orderRepository.save(order);

        eventPublisher.publish(new OrderPlacedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getSku(),
                order.getQuantity(),
                order.getUnitPrice()));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toResponse(order);
    }

    private static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getSku(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getStatus().name(),
                order.getCreatedAt());
    }
}

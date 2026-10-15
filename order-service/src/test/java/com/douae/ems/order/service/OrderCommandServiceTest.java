package com.douae.ems.order.service;

import com.douae.ems.common.dto.OrderResponse;
import com.douae.ems.common.dto.PlaceOrderRequest;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.order.domain.Order;
import com.douae.ems.order.messaging.OrderEventPublisher;
import com.douae.ems.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    private OrderCommandService service;

    @BeforeEach
    void setUp() {
        service = new OrderCommandService(orderRepository, eventPublisher);
    }

    @Test
    void placeOrderPersistsAndPublishesEvent() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PlaceOrderRequest request = new PlaceOrderRequest(
                "cust-1", "SKU-WIDGET-001", 2, new BigDecimal("19.99"));

        OrderResponse response = service.placeOrder(request);

        assertThat(response.orderId()).isNotBlank();
        assertThat(response.customerId()).isEqualTo("cust-1");
        assertThat(response.sku()).isEqualTo("SKU-WIDGET-001");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.status()).isEqualTo("PLACED");

        ArgumentCaptor<OrderPlacedEvent> captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getAggregateId()).isEqualTo(response.orderId());
        assertThat(captor.getValue().getSku()).isEqualTo("SKU-WIDGET-001");
    }

    @Test
    void getOrderThrowsWhenMissing() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getOrder("missing"))
                .isInstanceOf(OrderNotFoundException.class);
    }
}

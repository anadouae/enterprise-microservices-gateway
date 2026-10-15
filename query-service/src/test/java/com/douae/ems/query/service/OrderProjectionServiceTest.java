package com.douae.ems.query.service;

import com.douae.ems.common.dto.OrderViewResponse;
import com.douae.ems.common.event.InventoryRejectedEvent;
import com.douae.ems.common.event.InventoryReservedEvent;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.query.domain.OrderView;
import com.douae.ems.query.repository.OrderViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProjectionServiceTest {

    @Mock
    private OrderViewRepository orderViewRepository;

    private OrderProjectionService service;

    @BeforeEach
    void setUp() {
        service = new OrderProjectionService(orderViewRepository);
    }

    @Test
    void projectsOrderPlaced() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ord-1", "cust-1", "SKU-WIDGET-001", 2, new BigDecimal("10.00"));
        when(orderViewRepository.findById("ord-1")).thenReturn(Optional.empty());
        when(orderViewRepository.save(any(OrderView.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onOrderPlaced(event);

        ArgumentCaptor<OrderView> captor = ArgumentCaptor.forClass(OrderView.class);
        verify(orderViewRepository).save(captor.capture());
        assertThat(captor.getValue().getFulfilmentStatus()).isEqualTo("PENDING_INVENTORY");
        assertThat(captor.getValue().getCustomerId()).isEqualTo("cust-1");
    }

    @Test
    void projectsInventoryReservedOntoExistingView() {
        OrderView existing = OrderView.fromPlaced(
                "ord-1", "cust-1", "SKU-WIDGET-001", 2, new BigDecimal("10.00"),
                java.time.Instant.now());
        when(orderViewRepository.findById("ord-1")).thenReturn(Optional.of(existing));
        when(orderViewRepository.save(any(OrderView.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onInventoryReserved(new InventoryReservedEvent("ord-1", "SKU-WIDGET-001", 2, 8));

        assertThat(existing.getFulfilmentStatus()).isEqualTo("RESERVED");
        assertThat(existing.getRemainingStock()).isEqualTo(8);
    }

    @Test
    void projectsInventoryRejected() {
        when(orderViewRepository.findById("ord-2")).thenReturn(Optional.empty());
        when(orderViewRepository.save(any(OrderView.class))).thenAnswer(inv -> inv.getArgument(0));

        service.onInventoryRejected(new InventoryRejectedEvent(
                "ord-2", "SKU-NOPE", 1, 0, "UNKNOWN_SKU"));

        ArgumentCaptor<OrderView> captor = ArgumentCaptor.forClass(OrderView.class);
        verify(orderViewRepository).save(captor.capture());
        assertThat(captor.getValue().getFulfilmentStatus()).isEqualTo("REJECTED");
        assertThat(captor.getValue().getRejectionReason()).isEqualTo("UNKNOWN_SKU");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(orderViewRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById("missing"))
                .isInstanceOf(OrderViewNotFoundException.class);
    }

    @Test
    void byCustomerReturnsMappedViews() {
        OrderView view = OrderView.fromPlaced(
                "ord-1", "cust-1", "SKU-A", 1, BigDecimal.ONE, java.time.Instant.now());
        when(orderViewRepository.findByCustomerIdOrderByLastUpdatedAtDesc("cust-1"))
                .thenReturn(List.of(view));

        List<OrderViewResponse> result = service.byCustomer("cust-1");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().orderId()).isEqualTo("ord-1");
    }
}

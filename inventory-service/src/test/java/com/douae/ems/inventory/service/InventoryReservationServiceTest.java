package com.douae.ems.inventory.service;

import com.douae.ems.common.event.InventoryRejectedEvent;
import com.douae.ems.common.event.InventoryReservedEvent;
import com.douae.ems.common.event.OrderPlacedEvent;
import com.douae.ems.inventory.domain.StockItem;
import com.douae.ems.inventory.messaging.InventoryEventPublisher;
import com.douae.ems.inventory.repository.ProcessedEventRepository;
import com.douae.ems.inventory.repository.StockItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    private InventoryReservationService service;

    @BeforeEach
    void setUp() {
        service = new InventoryReservationService(
                stockItemRepository, processedEventRepository, eventPublisher);
    }

    @Test
    void reservesStockWhenAvailable() {
        OrderPlacedEvent event = placed("SKU-WIDGET-001", 3);
        when(processedEventRepository.existsById(event.getEventId().toString())).thenReturn(false);
        when(stockItemRepository.findById("SKU-WIDGET-001"))
                .thenReturn(Optional.of(new StockItem("SKU-WIDGET-001", 10)));

        service.handleOrderPlaced(event);

        ArgumentCaptor<InventoryReservedEvent> captor =
                ArgumentCaptor.forClass(InventoryReservedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getRemainingStock()).isEqualTo(7);
        verify(processedEventRepository).save(any());
    }

    @Test
    void rejectsWhenInsufficientStock() {
        OrderPlacedEvent event = placed("SKU-RARE-003", 5);
        when(processedEventRepository.existsById(event.getEventId().toString())).thenReturn(false);
        when(stockItemRepository.findById("SKU-RARE-003"))
                .thenReturn(Optional.of(new StockItem("SKU-RARE-003", 2)));

        service.handleOrderPlaced(event);

        ArgumentCaptor<InventoryRejectedEvent> captor =
                ArgumentCaptor.forClass(InventoryRejectedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getReason()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void skipsDuplicateEvents() {
        OrderPlacedEvent event = placed("SKU-WIDGET-001", 1);
        when(processedEventRepository.existsById(event.getEventId().toString())).thenReturn(true);

        service.handleOrderPlaced(event);

        verify(eventPublisher, never()).publish(any());
        verify(stockItemRepository, never()).findById(any());
    }

    @Test
    void rejectsUnknownSku() {
        OrderPlacedEvent event = placed("SKU-NOPE", 1);
        when(processedEventRepository.existsById(event.getEventId().toString())).thenReturn(false);
        when(stockItemRepository.findById("SKU-NOPE")).thenReturn(Optional.empty());

        service.handleOrderPlaced(event);

        ArgumentCaptor<InventoryRejectedEvent> captor =
                ArgumentCaptor.forClass(InventoryRejectedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getReason()).isEqualTo("UNKNOWN_SKU");
    }

    private static OrderPlacedEvent placed(String sku, int qty) {
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID().toString(), "cust-1", sku, qty, new BigDecimal("9.99"));
        return event;
    }
}

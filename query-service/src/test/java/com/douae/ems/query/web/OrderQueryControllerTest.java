package com.douae.ems.query.web;

import com.douae.ems.common.dto.OrderViewResponse;
import com.douae.ems.query.service.OrderProjectionService;
import com.douae.ems.query.service.OrderViewNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderQueryController.class)
class OrderQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderProjectionService projectionService;

    @Test
    void getView() throws Exception {
        when(projectionService.getById("ord-1")).thenReturn(new OrderViewResponse(
                "ord-1", "cust-1", "SKU-A", 1, BigDecimal.TEN, "RESERVED",
                9, null, Instant.parse("2024-01-01T00:00:00Z")));

        mockMvc.perform(get("/api/views/orders/ord-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fulfilmentStatus").value("RESERVED"));
    }

    @Test
    void listByCustomer() throws Exception {
        when(projectionService.byCustomer("cust-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/views/orders").param("customerId", "cust-1"))
                .andExpect(status().isOk());
    }

    @Test
    void notFound() throws Exception {
        when(projectionService.getById("x")).thenThrow(new OrderViewNotFoundException("x"));

        mockMvc.perform(get("/api/views/orders/x"))
                .andExpect(status().isNotFound());
    }
}

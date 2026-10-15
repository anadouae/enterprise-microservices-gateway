package com.douae.ems.order.web;

import com.douae.ems.common.dto.OrderResponse;
import com.douae.ems.common.dto.PlaceOrderRequest;
import com.douae.ems.order.service.OrderCommandService;
import com.douae.ems.order.service.OrderNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderCommandService orderCommandService;

    @Test
    void placeOrderReturnsCreated() throws Exception {
        when(orderCommandService.placeOrder(any(PlaceOrderRequest.class)))
                .thenReturn(new OrderResponse(
                        "ord-1", "cust-1", "SKU-WIDGET-001", 1,
                        new BigDecimal("10.00"), "PLACED", Instant.parse("2024-01-01T00:00:00Z")));

        PlaceOrderRequest body = new PlaceOrderRequest(
                "cust-1", "SKU-WIDGET-001", 1, new BigDecimal("10.00"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ord-1"));
    }

    @Test
    void placeOrderRejectsInvalidPayload() throws Exception {
        String invalid = "{\"customerId\":\"\",\"sku\":\"X\",\"quantity\":0,\"unitPrice\":0}";
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderNotFound() throws Exception {
        when(orderCommandService.getOrder("missing"))
                .thenThrow(new OrderNotFoundException("missing"));

        mockMvc.perform(get("/api/orders/missing"))
                .andExpect(status().isNotFound());
    }
}

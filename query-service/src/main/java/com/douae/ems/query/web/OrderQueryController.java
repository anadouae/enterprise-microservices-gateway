package com.douae.ems.query.web;

import com.douae.ems.common.dto.OrderViewResponse;
import com.douae.ems.query.service.OrderProjectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/views/orders")
public class OrderQueryController {

    private final OrderProjectionService projectionService;

    public OrderQueryController(OrderProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @GetMapping("/{orderId}")
    public OrderViewResponse get(@PathVariable String orderId) {
        return projectionService.getById(orderId);
    }

    @GetMapping
    public List<OrderViewResponse> byCustomer(@RequestParam String customerId) {
        return projectionService.byCustomer(customerId);
    }
}

package com.douae.ems.order.web;

import com.douae.ems.common.dto.OrderResponse;
import com.douae.ems.common.dto.PlaceOrderRequest;
import com.douae.ems.order.service.OrderCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommandService orderCommandService;

    public OrderController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest request) {
        return orderCommandService.placeOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        return orderCommandService.getOrder(orderId);
    }
}

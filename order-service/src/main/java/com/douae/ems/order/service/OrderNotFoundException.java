package com.douae.ems.order.service;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}

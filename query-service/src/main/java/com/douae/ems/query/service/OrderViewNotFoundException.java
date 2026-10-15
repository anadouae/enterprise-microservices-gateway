package com.douae.ems.query.service;

public class OrderViewNotFoundException extends RuntimeException {

    public OrderViewNotFoundException(String orderId) {
        super("Order view not found: " + orderId);
    }
}

package com.douae.ems.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String orderId,
        String customerId,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        String status,
        Instant createdAt
) {
}

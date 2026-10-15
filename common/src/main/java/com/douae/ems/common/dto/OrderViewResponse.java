package com.douae.ems.common.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** CQRS read-model projection returned by query-service. */
public record OrderViewResponse(
        String orderId,
        String customerId,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        String fulfilmentStatus,
        Integer remainingStock,
        String rejectionReason,
        Instant lastUpdatedAt
) {
}

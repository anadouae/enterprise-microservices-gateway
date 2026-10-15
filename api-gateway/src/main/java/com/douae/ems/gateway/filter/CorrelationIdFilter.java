package com.douae.ems.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends AbstractGatewayFilterFactory<CorrelationIdFilter.Config> {

    public static final String HEADER = "X-Correlation-Id";

    public CorrelationIdFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            List<String> existing = exchange.getRequest().getHeaders().get(HEADER);
            String correlationId = (existing != null && !existing.isEmpty())
                    ? existing.getFirst()
                    : UUID.randomUUID().toString();

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(HEADER, correlationId)
                    .build();

            exchange.getResponse().getHeaders().add(HEADER, correlationId);
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    public static class Config {
    }
}

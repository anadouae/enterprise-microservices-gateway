package com.douae.ems.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    @Test
    void injectsCorrelationIdWhenMissing() {
        CorrelationIdFilter filterFactory = new CorrelationIdFilter();
        GatewayFilter filter = filterFactory.apply(new CorrelationIdFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER))
                .isNotBlank();
        verify(chain).filter(any());
    }

    @Test
    void preservesExistingCorrelationId() {
        CorrelationIdFilter filterFactory = new CorrelationIdFilter();
        GatewayFilter filter = filterFactory.apply(new CorrelationIdFilter.Config());

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header(CorrelationIdFilter.HEADER, "fixed-id")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER))
                .isEqualTo("fixed-id");
    }
}

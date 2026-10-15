package com.douae.ems.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiKeyAuthFilterTest {

    @Test
    void disabledWhenApiKeyNotConfigured() {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/orders").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsMissingKeyWith401() {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("secret-key");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/orders").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rejectsWrongKeyWith401() {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("secret-key");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/orders")
                        .header(ApiKeyAuthFilter.HEADER, "wrong")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void allowsMatchingKey() {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("secret-key");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/orders")
                        .header(ApiKeyAuthFilter.HEADER, "secret-key")
                        .build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void allowsActuatorHealthWithoutKey() {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("secret-key");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }
}

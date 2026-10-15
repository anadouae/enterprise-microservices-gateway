package com.douae.ems.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Enforces {@code X-API-Key} when {@code GATEWAY_API_KEY} / {@code ems.gateway.api-key} is set.
 * Disabled when the key is blank so local unit tests and unsecured local runs stay simple.
 * Actuator health/info stay open for probes.
 */
@Component
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-API-Key";

    private final String configuredApiKey;

    public ApiKeyAuthFilter(@Value("${ems.gateway.api-key:}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey == null ? "" : configuredApiKey.trim();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!StringUtils.hasText(configuredApiKey)) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (isOpenPath(path)) {
            return chain.filter(exchange);
        }

        List<String> presented = exchange.getRequest().getHeaders().get(HEADER);
        String candidate = (presented != null && !presented.isEmpty()) ? presented.getFirst() : null;
        if (candidate != null && constantTimeEquals(candidate, configuredApiKey)) {
            return chain.filter(exchange);
        }

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private static boolean isOpenPath(String path) {
        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.equals("/actuator");
    }

    /** Avoid short-circuit leaks on timing of string equals for API keys. */
    private static boolean constantTimeEquals(String a, String b) {
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    @Override
    public int getOrder() {
        return -200;
    }
}

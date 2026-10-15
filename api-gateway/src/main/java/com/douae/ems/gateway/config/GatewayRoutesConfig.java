package com.douae.ems.gateway.config;

import com.douae.ems.gateway.filter.CorrelationIdFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declarative ingress routing: commands vs queries stay on separate backends.
 *
 * <p><b>Complexity note:</b> path-prefix match is O(n) over declared routes (n is tiny).
 * Correlation filter adds constant-time header injection per request — no I/O.
 */
@Configuration
public class GatewayRoutesConfig {

    @Value("${ems.services.order-service-uri:http://localhost:8081}")
    private String orderServiceUri;

    @Value("${ems.services.query-service-uri:http://localhost:8083}")
    private String queryServiceUri;

    @Value("${ems.services.inventory-service-uri:http://localhost:8082}")
    private String inventoryServiceUri;

    @Bean
    public RouteLocator emsRoutes(RouteLocatorBuilder builder, CorrelationIdFilter correlationIdFilter) {
        return builder.routes()
                .route("order-commands", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(correlationIdFilter.apply(new CorrelationIdFilter.Config())))
                        .uri(orderServiceUri))
                .route("order-views", r -> r
                        .path("/api/views/**")
                        .filters(f -> f.filter(correlationIdFilter.apply(new CorrelationIdFilter.Config())))
                        .uri(queryServiceUri))
                .route("stock-queries", r -> r
                        .path("/api/stock/**")
                        .filters(f -> f.filter(correlationIdFilter.apply(new CorrelationIdFilter.Config())))
                        .uri(inventoryServiceUri))
                .build();
    }
}

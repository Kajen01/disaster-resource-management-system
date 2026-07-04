package com.drms.apigateway.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Correlation-Id"))
                .orElse(UUID.randomUUID().toString());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Correlation-Id", correlationId)
                .build();
        exchange.getResponse().getHeaders().set("X-Correlation-Id", correlationId);
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -2;
    }
}

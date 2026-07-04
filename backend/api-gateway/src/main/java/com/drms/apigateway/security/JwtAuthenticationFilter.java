package com.drms.apigateway.security;

import com.drms.apigateway.config.RouteSecurityProperties;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final RouteSecurityProperties properties;
    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(RouteSecurityProperties properties, JwtService jwtService) {
        this.properties = properties;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = header.substring(7);
        try {
            List<String> requiredRoles = rolesForPath(path, method);
            if (!requiredRoles.isEmpty() && !jwtService.hasRequiredRole(token, requiredRoles)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Email", jwtService.extractSubject(token))
                    .header("X-User-Role", jwtService.extractRole(token))
                    .header("X-User-Id", jwtService.extractUserId(token))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return properties.publicPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private List<String> rolesForPath(String path, HttpMethod method) {
        if (pathMatcher.match("/api/users/me", path)) {
            return List.of("ADMIN", "SHELTER_MANAGER", "DONOR");
        }
        if (HttpMethod.GET.equals(method) && pathMatcher.match("/api/shelters/**", path)) {
            return List.of("ADMIN", "SHELTER_MANAGER", "DONOR");
        }
        return properties.roleRules().stream()
                .filter(rule -> pathMatcher.match(rule.path(), path))
                .findFirst()
                .map(RouteSecurityProperties.RoleRule::roles)
                .orElse(List.of());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

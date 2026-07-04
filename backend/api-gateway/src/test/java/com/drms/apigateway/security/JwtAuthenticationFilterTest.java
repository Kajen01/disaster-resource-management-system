package com.drms.apigateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.drms.apigateway.config.RouteSecurityProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    @Test
    void allowsOptionsRequestsToPassThroughForCorsPreflight() {
        RouteSecurityProperties properties = new RouteSecurityProperties(
                "ZHJtcy1zaGFyZWQtc2VjcmV0LWRybXMtc2hhcmVkLXNlY3JldC1kcm1zLXNoYXJlZC1zZWNyZXQ=",
                List.of("/api/auth/login"),
                List.of()
        );
        JwtService jwtService = Mockito.mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(properties, jwtService);
        GatewayFilterChain chain = Mockito.mock(GatewayFilterChain.class);

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/users/5/status")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PATCH")
                        .build()
        );
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        assertNotNull(result);
        verify(chain).filter(exchange);
        verify(jwtService, never()).hasRequiredRole(Mockito.anyString(), Mockito.anyList());
    }

    @Test
    void rejectsProtectedRequestsWithoutBearerToken() {
        RouteSecurityProperties properties = new RouteSecurityProperties(
                "ZHJtcy1zaGFyZWQtc2VjcmV0LWRybXMtc2hhcmVkLXNlY3JldC1kcm1zLXNoYXJlZC1zZWNyZXQ=",
                List.of("/api/auth/login"),
                List.of(new RouteSecurityProperties.RoleRule("/api/users/**", List.of("ADMIN")))
        );
        JwtService jwtService = Mockito.mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(properties, jwtService);
        GatewayFilterChain chain = Mockito.mock(GatewayFilterChain.class);

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header("Origin", "http://localhost:5173")
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void allowsDonorRoleForShelterReadRequests() {
        RouteSecurityProperties properties = new RouteSecurityProperties(
                "ZHJtcy1zaGFyZWQtc2VjcmV0LWRybXMtc2hhcmVkLXNlY3JldC1kcm1zLXNoYXJlZC1zZWNyZXQ=",
                List.of("/api/auth/login"),
                List.of(new RouteSecurityProperties.RoleRule("/api/shelters/**", List.of("ADMIN", "SHELTER_MANAGER")))
        );
        JwtService jwtService = Mockito.mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(properties, jwtService);
        GatewayFilterChain chain = Mockito.mock(GatewayFilterChain.class);

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/shelters")
                        .header("Authorization", "Bearer donor-token")
                        .build()
        );

        when(jwtService.hasRequiredRole("donor-token", List.of("ADMIN", "SHELTER_MANAGER", "DONOR"))).thenReturn(true);
        when(jwtService.extractSubject("donor-token")).thenReturn("donor@example.com");
        when(jwtService.extractRole("donor-token")).thenReturn("DONOR");
        when(jwtService.extractUserId("donor-token")).thenReturn("42");
        when(chain.filter(Mockito.any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertNull(exchange.getResponse().getStatusCode());
        verify(jwtService).hasRequiredRole("donor-token", List.of("ADMIN", "SHELTER_MANAGER", "DONOR"));
        verify(chain).filter(Mockito.any());
    }
}

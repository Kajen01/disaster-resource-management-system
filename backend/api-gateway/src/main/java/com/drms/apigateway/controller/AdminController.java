package com.drms.apigateway.controller;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final List<ServiceDescriptor> SERVICES = List.of(
            new ServiceDescriptor("api-gateway", "http://localhost:8080/actuator/health"),
            new ServiceDescriptor("config-server", "http://CONFIG-SERVER/actuator/health"),
            new ServiceDescriptor("service-registry", "http://SERVICE-REGISTRY/actuator/health"),
            new ServiceDescriptor("user-service", "http://USER-SERVICE/actuator/health"),
            new ServiceDescriptor("shelter-service", "http://SHELTER-SERVICE/actuator/health"),
            new ServiceDescriptor("resource-service", "http://RESOURCE-SERVICE/actuator/health"),
            new ServiceDescriptor("sharing-transparency-service", "http://SHARING-TRANSPARENCY-SERVICE/actuator/health")
    );

    private final WebClient.Builder webClientBuilder;

    public AdminController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/health")
    public Mono<List<ServiceHealthResponse>> getHealth() {
        return Flux.fromIterable(SERVICES)
                .flatMap(this::fetchHealth)
                .collectList();
    }

    private Mono<ServiceHealthResponse> fetchHealth(ServiceDescriptor descriptor) {
        return webClientBuilder.build()
                .get()
                .uri(descriptor.healthUrl())
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(4))
                .map(body -> new ServiceHealthResponse(
                        descriptor.name(),
                        String.valueOf(body.getOrDefault("status", "UNKNOWN")),
                        true
                ))
                .onErrorResume(ex -> Mono.just(new ServiceHealthResponse(descriptor.name(), "DOWN", false)));
    }

    private record ServiceDescriptor(String name, String healthUrl) {
    }

    public record ServiceHealthResponse(String serviceName, String status, boolean reachable) {
    }
}

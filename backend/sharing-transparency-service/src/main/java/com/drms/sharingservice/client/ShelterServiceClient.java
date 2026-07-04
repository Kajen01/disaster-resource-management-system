package com.drms.sharingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shelter-service")
public interface ShelterServiceClient {

    @GetMapping("/api/shelters/{id}/availability")
    boolean isAvailable(@PathVariable("id") Long shelterId);
}

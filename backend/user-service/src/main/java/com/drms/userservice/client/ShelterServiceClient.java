package com.drms.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "shelter-service")
public interface ShelterServiceClient {

    @PostMapping("/api/shelters")
    ShelterResponse create(@RequestBody ShelterRequest request);

    @PatchMapping("/api/shelters/internal/manager/{managerUserId}/status")
    void updateStatusByManager(
            @PathVariable("managerUserId") Long managerUserId,
            @RequestParam("status") String status
    );

    record ShelterRequest(
            String name,
            String district,
            String addressLine1,
            String addressLine2,
            String contactName,
            String contactPhone,
            Long managerUserId,
            String status
    ) {}

    record ShelterResponse(
            Long id,
            String name,
            String status
    ) {}
}

package com.drms.shelterservice.controller;

import com.drms.shelterservice.dto.CapacityUpdateRequest;
import com.drms.shelterservice.dto.OccupancyUpdateRequest;
import com.drms.shelterservice.dto.ShelterRequest;
import com.drms.shelterservice.dto.ShelterResponse;
import com.drms.shelterservice.service.ShelterService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.drms.shelterservice.entity.ShelterStatus;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/shelters")
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelterResponse create(@Valid @RequestBody ShelterRequest request) {
        return shelterService.create(request);
    }

    @GetMapping
    public List<ShelterResponse> getAll() {
        return shelterService.getAll();
    }

    @GetMapping("/{id}")
    public ShelterResponse getById(@PathVariable Long id) {
        return shelterService.getById(id);
    }

    @PatchMapping("/{id}")
    public ShelterResponse update(@PathVariable Long id, @Valid @RequestBody ShelterRequest request) {
        return shelterService.update(id, request);
    }

    @PatchMapping("/{id}/capacity")
    public ShelterResponse updateCapacity(@PathVariable Long id, @Valid @RequestBody CapacityUpdateRequest request) {
        return shelterService.updateCapacity(id, request);
    }

    @PatchMapping("/{id}/occupancy")
    public ShelterResponse updateOccupancy(@PathVariable Long id, @Valid @RequestBody OccupancyUpdateRequest request) {
        return shelterService.updateOccupancy(id, request);
    }

    @GetMapping("/{id}/availability")
    public boolean isAvailable(@PathVariable Long id) {
        return shelterService.isAvailable(id);
    }

    @PatchMapping("/internal/manager/{managerUserId}/status")
    public void updateStatusByManager(
            @PathVariable("managerUserId") Long managerUserId,
            @RequestParam("status") ShelterStatus status
    ) {
        shelterService.updateStatusByManager(managerUserId, status);
    }
}

package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ResourceCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        Long sourceShelterId,
        @NotNull Long targetShelterId,
        @NotNull ResourceCategory resourceType,
        @NotBlank String resourceName,
        @NotBlank String unit,
        @Min(1) int quantity,
        @NotBlank String referenceNumber,
        Long batchId
) {
    public ReservationRequest(Long sourceShelterId, Long targetShelterId, com.drms.resourceservice.entity.ResourceCategory resourceType, String resourceName, String unit, int quantity, String referenceNumber) {
        this(sourceShelterId, targetShelterId, resourceType, resourceName, unit, quantity, referenceNumber, null);
    }
}

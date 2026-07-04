package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ResourceCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BatchIntakeRequest(
        Long shelterId,
        @NotNull ResourceCategory resourceType,
        @NotBlank String resourceName,
        @NotBlank String unit,
        @Min(1) int quantityReceived,
        LocalDate expiryDate,
        String sourceDonationRef,
        String donorEmail
) {
    public BatchIntakeRequest(Long shelterId, ResourceCategory resourceType, String resourceName, String unit, int quantityReceived, LocalDate expiryDate, String sourceDonationRef) {
        this(shelterId, resourceType, resourceName, unit, quantityReceived, expiryDate, sourceDonationRef, null);
    }
}

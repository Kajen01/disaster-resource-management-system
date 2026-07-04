package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ResourceCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShortageAnalysisRequest(
        @NotNull Long shelterId,
        @NotNull ResourceCategory resourceType,
        @NotBlank String resourceName,
        @Min(1) int requiredQuantity
) {
}

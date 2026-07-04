package com.drms.sharingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShortageRequestDto(
        @NotNull Long shelterId,
        @NotBlank String resourceType,
        @NotBlank String resourceName,
        @NotBlank String unit,
        @Min(1) int requiredQuantity,
        String justification
) {
}

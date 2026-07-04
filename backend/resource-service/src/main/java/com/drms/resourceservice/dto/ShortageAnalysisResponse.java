package com.drms.resourceservice.dto;

public record ShortageAnalysisResponse(
        Long shelterId,
        String resourceName,
        int availableQuantity,
        int requiredQuantity,
        int shortageQuantity,
        boolean shortage
) {
}

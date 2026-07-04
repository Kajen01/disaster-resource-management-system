package com.drms.sharingservice.dto;

public record ExternalShortageAnalysisResponse(
        Long shelterId,
        String resourceName,
        int availableQuantity,
        int requiredQuantity,
        int shortageQuantity,
        boolean shortage
) {
}

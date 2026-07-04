package com.drms.sharingservice.dto;

public record ExternalShortageAnalysisRequest(
        Long shelterId,
        String resourceType,
        String resourceName,
        int requiredQuantity
) {
}

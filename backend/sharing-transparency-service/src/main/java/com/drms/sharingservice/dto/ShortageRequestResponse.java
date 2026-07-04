package com.drms.sharingservice.dto;

import java.time.Instant;

public record ShortageRequestResponse(
        Long id,
        Long shelterId,
        String resourceType,
        String resourceName,
        String unit,
        int requiredQuantity,
        int shortageQuantity,
        String justification,
        String status,
        Instant createdAt
) {
}

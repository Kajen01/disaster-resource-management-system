package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ResourceCategory;
import java.time.Instant;
import java.time.LocalDate;

public record BatchResponse(
        Long id,
        Long shelterId,
        String donorEmail,
        ResourceCategory resourceType,
        String resourceName,
        String unit,
        int quantityReceived,
        int quantityAvailable,
        LocalDate expiryDate,
        Instant receivedAt,
        String sourceDonationRef
) {
}

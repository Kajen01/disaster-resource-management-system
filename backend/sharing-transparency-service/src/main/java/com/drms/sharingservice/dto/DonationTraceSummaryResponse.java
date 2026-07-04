package com.drms.sharingservice.dto;

import java.time.Instant;

public record DonationTraceSummaryResponse(
        Long transferId,
        String donationRef,
        Long sourceShelterId,
        Long destinationShelterId,
        String resourceType,
        String resourceName,
        int quantity,
        Instant recordedAt
) {
}

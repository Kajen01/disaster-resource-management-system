package com.drms.sharingservice.dto;

public record ExcessNotificationRequest(
        Long shelterId,
        Long batchId,
        String sourceDonationRef,
        String resourceType,
        String resourceName,
        String unit,
        int quantity
) {
}

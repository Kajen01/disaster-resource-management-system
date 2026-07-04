package com.drms.sharingservice.dto;

public record AdminTransferRequest(
        Long shortageRequestId,
        Long targetShelterId,
        String resourceType,
        String resourceName,
        String unit,
        int quantity,
        Long batchId,
        String donationRef
) {
}

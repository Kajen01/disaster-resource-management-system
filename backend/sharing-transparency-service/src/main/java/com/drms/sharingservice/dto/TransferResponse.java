package com.drms.sharingservice.dto;

public record TransferResponse(
        Long transferId,
        Long shortageRequestId,
        Long sourceShelterId,
        Long targetShelterId,
        Long reservationId,
        Long sourceBatchId,
        String donationRef,
        String resourceType,
        String resourceName,
        String unit,
        int quantity,
        String status
) {
}

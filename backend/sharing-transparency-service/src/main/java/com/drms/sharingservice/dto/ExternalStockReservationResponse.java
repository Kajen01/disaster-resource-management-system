package com.drms.sharingservice.dto;

public record ExternalStockReservationResponse(
        Long reservationId,
        Long batchId,
        Long sourceShelterId,
        Long targetShelterId,
        String resourceType,
        String resourceName,
        String unit,
        int reservedQuantity,
        String referenceNumber,
        String status
) {
}

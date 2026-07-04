package com.drms.sharingservice.dto;

public record ExternalReservationRequest(
        Long sourceShelterId,
        Long targetShelterId,
        String resourceType,
        String resourceName,
        String unit,
        int quantity,
        String referenceNumber,
        Long batchId
) {
    public ExternalReservationRequest(Long sourceShelterId, Long targetShelterId, String resourceType, String resourceName, String unit, int quantity, String referenceNumber) {
        this(sourceShelterId, targetShelterId, resourceType, resourceName, unit, quantity, referenceNumber, null);
    }
}

package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ReservationStatus;
import com.drms.resourceservice.entity.ResourceCategory;
import java.time.Instant;

public record StockReservationResponse(
        Long reservationId,
        Long batchId,
        Long sourceShelterId,
        Long targetShelterId,
        ResourceCategory resourceType,
        String resourceName,
        String unit,
        int reservedQuantity,
        String referenceNumber,
        ReservationStatus status,
        Instant createdAt
) {
}

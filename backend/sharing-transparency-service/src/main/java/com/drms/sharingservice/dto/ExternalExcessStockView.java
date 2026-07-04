package com.drms.sharingservice.dto;

import java.time.LocalDate;

public record ExternalExcessStockView(
        Long batchId,
        Long shelterId,
        String resourceType,
        String resourceName,
        String unit,
        int quantityAvailable,
        LocalDate expiryDate,
        String sourceDonationRef
) {
}

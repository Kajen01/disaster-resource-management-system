package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ResourceCategory;
import java.time.LocalDate;

public record ExcessStockView(
        Long batchId,
        Long shelterId,
        ResourceCategory resourceType,
        String resourceName,
        String unit,
        int quantityAvailable,
        LocalDate expiryDate,
        String sourceDonationRef
) {
}

package com.drms.resourceservice.dto;

import com.drms.resourceservice.entity.ResourceCategory;
import java.time.LocalDate;
import java.util.List;

public record BulkIntakeRequest(
        Long shelterId,
        String donorEmail,
        List<IntakeItem> items
) {
    public record IntakeItem(
            ResourceCategory resourceType,
            String resourceName,
            String unit,
            int quantityReceived,
            LocalDate expiryDate
    ) {}
}

package com.drms.sharingservice.dto;

import java.util.List;

public record BulkExcessNotificationRequest(
        Long shelterId,
        List<ExcessItem> items
) {
    public record ExcessItem(
            Long batchId,
            String sourceDonationRef,
            String resourceType,
            String resourceName,
            String unit,
            int quantity
    ) {}
}

package com.drms.resourceservice.dto;

import java.util.List;

public record DonationHistoryResponse(
        String donorEmail,
        int totalBatches,
        List<BatchResponse> batches
) {
}

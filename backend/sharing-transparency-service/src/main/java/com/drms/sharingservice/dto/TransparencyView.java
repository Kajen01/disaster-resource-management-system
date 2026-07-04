package com.drms.sharingservice.dto;

import java.util.List;

public record TransparencyView(
        String donationRef,
        String resourceType,
        String resourceName,
        Long sourceShelterId,
        Long destinationShelterId,
        int quantity,
        List<TransparencyTimelineEventDto> timeline
) {
}

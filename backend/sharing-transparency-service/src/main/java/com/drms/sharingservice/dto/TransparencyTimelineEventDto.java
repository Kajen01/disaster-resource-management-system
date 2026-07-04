package com.drms.sharingservice.dto;

import java.time.Instant;

public record TransparencyTimelineEventDto(String eventType, String details, Instant occurredAt) {
}

package com.drms.shelterservice.dto;

import jakarta.validation.constraints.Min;

public record CapacityUpdateRequest(@Min(1) int capacity) {
}

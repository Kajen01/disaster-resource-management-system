package com.drms.shelterservice.dto;

import jakarta.validation.constraints.Min;

public record OccupancyUpdateRequest(@Min(0) int occupancy) {
}

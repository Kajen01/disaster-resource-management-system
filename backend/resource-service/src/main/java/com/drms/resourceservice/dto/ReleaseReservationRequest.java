package com.drms.resourceservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ReleaseReservationRequest(@NotBlank String reason) {
}

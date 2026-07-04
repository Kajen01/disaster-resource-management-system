package com.drms.resourceservice.dto;

import jakarta.validation.constraints.NotNull;

public record TransferConfirmRequest(
        @NotNull Long reservationId,
        @NotNull Long receivingShelterId
) {
}

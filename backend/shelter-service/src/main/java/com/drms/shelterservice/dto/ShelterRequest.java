package com.drms.shelterservice.dto;

import com.drms.shelterservice.entity.ShelterStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShelterRequest(
        @NotBlank String name,
        @NotBlank String district,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String contactName,
        @NotBlank String contactPhone,
        @NotNull Long managerUserId,
        @NotNull ShelterStatus status
) {
}

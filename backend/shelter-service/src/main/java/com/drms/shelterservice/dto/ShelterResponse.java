package com.drms.shelterservice.dto;

import com.drms.shelterservice.entity.ShelterStatus;

public record ShelterResponse(
        Long id,
        String name,
        String district,
        String addressLine1,
        String addressLine2,
        String contactName,
        String contactPhone,
        Long managerUserId,
        ShelterStatus status
) {
}

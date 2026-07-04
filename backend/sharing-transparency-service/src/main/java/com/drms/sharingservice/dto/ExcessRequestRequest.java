package com.drms.sharingservice.dto;

public record ExcessRequestRequest(
        Long requestingShelterId,
        int quantity
) {
}

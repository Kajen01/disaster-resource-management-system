package com.drms.shelterservice.mapper;

import com.drms.shelterservice.dto.ShelterResponse;
import com.drms.shelterservice.entity.Shelter;
import org.springframework.stereotype.Component;

@Component
public class ShelterMapper {

    public ShelterResponse toResponse(Shelter shelter) {
        return new ShelterResponse(
                shelter.getId(),
                shelter.getName(),
                shelter.getDistrict(),
                shelter.getAddressLine1(),
                shelter.getAddressLine2(),
                shelter.getContactName(),
                shelter.getContactPhone(),
                shelter.getManagerUserId(),
                shelter.getStatus()
        );
    }
}

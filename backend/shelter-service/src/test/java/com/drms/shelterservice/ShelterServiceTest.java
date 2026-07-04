package com.drms.shelterservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import com.drms.shelterservice.client.UserServiceClient;
import com.drms.shelterservice.dto.ShelterRequest;
import com.drms.shelterservice.entity.ShelterStatus;
import com.drms.shelterservice.exception.ConflictException;
import com.drms.shelterservice.mapper.ShelterMapper;
import com.drms.shelterservice.repository.ShelterRepository;
import com.drms.shelterservice.service.ShelterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShelterServiceTest {

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private ShelterMapper shelterMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ShelterService shelterService;

    @Test
    void createSavesShelterSuccessfully() {
        ShelterRequest request = new ShelterRequest(
                "Shelter A",
                "Colombo",
                "Main Street",
                "",
                "Manager",
                "0771234567",
                2L,
                ShelterStatus.INACTIVE
        );

        com.drms.shelterservice.entity.Shelter shelter = new com.drms.shelterservice.entity.Shelter();
        when(shelterRepository.save(any(com.drms.shelterservice.entity.Shelter.class))).thenReturn(shelter);

        shelterService.create(request);
    }
}

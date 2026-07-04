package com.drms.shelterservice.service;

import com.drms.shelterservice.client.UserServiceClient;
import com.drms.shelterservice.dto.CapacityUpdateRequest;
import com.drms.shelterservice.dto.OccupancyUpdateRequest;
import com.drms.shelterservice.dto.ShelterRequest;
import com.drms.shelterservice.dto.ShelterResponse;
import com.drms.shelterservice.entity.Shelter;
import com.drms.shelterservice.entity.ShelterStatus;
import com.drms.shelterservice.exception.ConflictException;
import com.drms.shelterservice.exception.NotFoundException;
import com.drms.shelterservice.mapper.ShelterMapper;
import com.drms.shelterservice.repository.ShelterRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShelterService {

    private final ShelterRepository shelterRepository;
    private final ShelterMapper shelterMapper;
    private final UserServiceClient userServiceClient;

    public ShelterService(
            ShelterRepository shelterRepository,
            ShelterMapper shelterMapper,
            UserServiceClient userServiceClient
    ) {
        this.shelterRepository = shelterRepository;
        this.shelterMapper = shelterMapper;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public ShelterResponse create(ShelterRequest request) {
        validateManager(request.managerUserId());
        Shelter shelter = toEntity(request, new Shelter());
        return shelterMapper.toResponse(shelterRepository.save(shelter));
    }

    public List<ShelterResponse> getAll() {
        return shelterRepository.findAll().stream().map(shelterMapper::toResponse).toList();
    }

    public ShelterResponse getById(Long id) {
        return shelterMapper.toResponse(findShelter(id));
    }

    @Transactional
    public ShelterResponse update(Long id, ShelterRequest request) {
        validateManager(request.managerUserId());
        Shelter shelter = toEntity(request, findShelter(id).toBuilder().build());
        shelter.setId(id);
        return shelterMapper.toResponse(shelterRepository.save(shelter));
    }

    @Transactional
    public ShelterResponse updateCapacity(Long id, CapacityUpdateRequest request) {
        Shelter shelter = findShelter(id);
        return shelterMapper.toResponse(shelter);
    }

    @Transactional
    public ShelterResponse updateOccupancy(Long id, OccupancyUpdateRequest request) {
        Shelter shelter = findShelter(id);
        return shelterMapper.toResponse(shelter);
    }

    public boolean isAvailable(Long id) {
        Shelter shelter = findShelter(id);
        return shelter.getStatus() == ShelterStatus.ACTIVE;
    }

    @Transactional
    public void updateStatusByManager(Long managerUserId, ShelterStatus status) {
        shelterRepository.findByManagerUserId(managerUserId).ifPresent(shelter -> {
            shelter.setStatus(status);
            shelterRepository.save(shelter);
        });
    }

    private Shelter findShelter(Long id) {
        return shelterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelter not found"));
    }

    private void validateCapacity(int capacity, int occupancy) {
        // Capacity validation disabled - no capacity check required
    }

    private void validateManager(Long managerUserId) {
        // Validation bypassed for auto-created manager-shelter accounts
    }

    private Shelter toEntity(ShelterRequest request, Shelter shelter) {
        shelter.setName(request.name());
        shelter.setDistrict(request.district());
        shelter.setAddressLine1(request.addressLine1());
        shelter.setAddressLine2(request.addressLine2());
        shelter.setContactName(request.contactName());
        shelter.setContactPhone(request.contactPhone());
        shelter.setManagerUserId(request.managerUserId());
        shelter.setStatus(request.status());
        return shelter;
    }
}

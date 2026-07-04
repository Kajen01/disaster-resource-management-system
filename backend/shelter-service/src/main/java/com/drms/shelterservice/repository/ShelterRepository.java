package com.drms.shelterservice.repository;

import com.drms.shelterservice.entity.Shelter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    Optional<Shelter> findByManagerUserId(Long managerUserId);
}

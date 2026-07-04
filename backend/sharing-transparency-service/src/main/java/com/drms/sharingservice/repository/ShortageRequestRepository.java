package com.drms.sharingservice.repository;

import com.drms.sharingservice.entity.ShortageRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortageRequestRepository extends JpaRepository<ShortageRequest, Long> {
    List<ShortageRequest> findByShelterIdOrderByCreatedAtDesc(Long shelterId);
    List<ShortageRequest> findAllByOrderByCreatedAtDesc();
}

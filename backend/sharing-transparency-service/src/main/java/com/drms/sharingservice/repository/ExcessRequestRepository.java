package com.drms.sharingservice.repository;

import com.drms.sharingservice.entity.ExcessRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcessRequestRepository extends JpaRepository<ExcessRequest, Long> {
    List<ExcessRequest> findByExcessNotificationId(Long excessNotificationId);
    List<ExcessRequest> findByRequestingShelterId(Long requestingShelterId);
}

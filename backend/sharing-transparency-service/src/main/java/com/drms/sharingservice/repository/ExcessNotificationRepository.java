package com.drms.sharingservice.repository;

import com.drms.sharingservice.entity.ExcessNotification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcessNotificationRepository extends JpaRepository<ExcessNotification, Long> {
    List<ExcessNotification> findByStatusOrderByCreatedAtDesc(String status);
    List<ExcessNotification> findByShelterIdOrderByCreatedAtDesc(Long shelterId);
}

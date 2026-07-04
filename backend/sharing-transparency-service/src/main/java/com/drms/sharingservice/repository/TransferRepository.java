package com.drms.sharingservice.repository;

import com.drms.sharingservice.entity.Transfer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByShortageRequestId(Long shortageRequestId);

    List<Transfer> findBySourceShelterIdOrderByCreatedAtDesc(Long sourceShelterId);

    List<Transfer> findByTargetShelterIdOrderByCreatedAtDesc(Long targetShelterId);
}

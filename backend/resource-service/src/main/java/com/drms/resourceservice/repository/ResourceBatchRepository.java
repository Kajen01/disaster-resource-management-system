package com.drms.resourceservice.repository;

import com.drms.resourceservice.entity.ResourceBatch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceBatchRepository extends JpaRepository<ResourceBatch, Long> {

    List<ResourceBatch> findByShelterIdOrderByReceivedAtDesc(Long shelterId);

    List<ResourceBatch> findByDonorEmailOrderByReceivedAtDesc(String donorEmail);

    List<ResourceBatch> findByResourceNameIgnoreCaseAndQuantityAvailableGreaterThan(String resourceName, int minimumAvailable);

    List<ResourceBatch> findByShelterIdAndResourceNameIgnoreCase(Long shelterId, String resourceName);

    List<ResourceBatch> findByShelterIdIsNullOrderByReceivedAtDesc();
}

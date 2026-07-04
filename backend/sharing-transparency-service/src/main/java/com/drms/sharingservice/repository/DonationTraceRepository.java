package com.drms.sharingservice.repository;

import com.drms.sharingservice.entity.DonationTrace;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationTraceRepository extends JpaRepository<DonationTrace, Long> {

    Optional<DonationTrace> findFirstByDonationRef(String donationRef);
}

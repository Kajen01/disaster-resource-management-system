package com.drms.sharingservice.repository;

import com.drms.sharingservice.entity.TransparencyTimelineEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransparencyTimelineEventRepository extends JpaRepository<TransparencyTimelineEvent, Long> {

    List<TransparencyTimelineEvent> findByDonationRefOrderByOccurredAtAsc(String donationRef);
}

package com.drms.sharingservice.service;

import com.drms.sharingservice.entity.DonationTrace;
import com.drms.sharingservice.entity.TransparencyTimelineEvent;
import com.drms.sharingservice.repository.DonationTraceRepository;
import com.drms.sharingservice.repository.TransparencyTimelineEventRepository;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final DonationTraceRepository donationTraceRepository;
    private final TransparencyTimelineEventRepository eventRepository;

    public AuditEventListener(
            DonationTraceRepository donationTraceRepository,
            TransparencyTimelineEventRepository eventRepository
    ) {
        this.donationTraceRepository = donationTraceRepository;
        this.eventRepository = eventRepository;
    }

    @RabbitListener(queues = "drms.sharing.audit")
    @Transactional
    public void onEvent(Map<String, Object> event) {
        log.info("Sharing service observed event: {}", event);
        try {
            String donationRef = (String) event.get("donationRef");
            if (donationRef == null) {
                return;
            }

            if (event.containsKey("batchId") && event.containsKey("quantity") && event.containsKey("resourceType")) {
                String resourceName = (String) event.get("resourceName");
                String resourceType = (String) event.get("resourceType");
                String donorEmail = (String) event.get("donorEmail");
                int quantity = ((Number) event.get("quantity")).intValue();
                Number shelterIdNum = (Number) event.get("shelterId");
                Long shelterId = shelterIdNum != null ? shelterIdNum.longValue() : 0L;

                if (!donationTraceRepository.findFirstByDonationRef(donationRef).isPresent()) {
                    donationTraceRepository.save(DonationTrace.builder()
                            .donationRef(donationRef)
                            .resourceType(resourceType)
                            .resourceName(resourceName)
                            .quantity(quantity)
                            .sourceShelterId(shelterId)
                            .destinationShelterId(shelterId)
                            .recordedAt(Instant.now())
                            .build());
                }

                String desc = "Donation of " + quantity + " units of " + resourceName + " registered by Admin" 
                        + (donorEmail != null && !donorEmail.isBlank() ? " from donor " + donorEmail : "");
                eventRepository.save(TransparencyTimelineEvent.builder()
                        .donationRef(donationRef)
                        .eventType("DONATION_LOGGED")
                        .details(desc)
                        .occurredAt(Instant.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to handle audit event", e);
        }
    }
}

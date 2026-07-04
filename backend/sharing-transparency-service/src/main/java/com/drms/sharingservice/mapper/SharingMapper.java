package com.drms.sharingservice.mapper;

import com.drms.sharingservice.dto.ShortageRequestResponse;
import com.drms.sharingservice.dto.TransferResponse;
import com.drms.sharingservice.dto.DonationTraceSummaryResponse;
import com.drms.sharingservice.dto.TransparencyTimelineEventDto;
import com.drms.sharingservice.entity.DonationTrace;
import com.drms.sharingservice.entity.ShortageRequest;
import com.drms.sharingservice.entity.Transfer;
import com.drms.sharingservice.entity.TransparencyTimelineEvent;
import org.springframework.stereotype.Component;

@Component
public class SharingMapper {

    public ShortageRequestResponse toShortageResponse(ShortageRequest shortageRequest) {
        return new ShortageRequestResponse(
                shortageRequest.getId(),
                shortageRequest.getShelterId(),
                shortageRequest.getResourceType(),
                shortageRequest.getResourceName(),
                shortageRequest.getUnit(),
                shortageRequest.getRequiredQuantity(),
                shortageRequest.getShortageQuantity(),
                shortageRequest.getJustification(),
                shortageRequest.getStatus().name(),
                shortageRequest.getCreatedAt()
        );
    }

    public TransferResponse toTransferResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getShortageRequestId(),
                transfer.getSourceShelterId(),
                transfer.getTargetShelterId(),
                transfer.getReservationId(),
                transfer.getSourceBatchId(),
                transfer.getDonationRef(),
                transfer.getResourceType(),
                transfer.getResourceName(),
                transfer.getUnit(),
                transfer.getQuantity(),
                transfer.getStatus().name()
        );
    }

    public TransparencyTimelineEventDto toTimelineEvent(TransparencyTimelineEvent event) {
        return new TransparencyTimelineEventDto(event.getEventType(), event.getDetails(), event.getOccurredAt());
    }

    public DonationTraceSummaryResponse toDonationTraceSummary(DonationTrace trace) {
        return new DonationTraceSummaryResponse(
                trace.getTransferId(),
                trace.getDonationRef(),
                trace.getSourceShelterId(),
                trace.getDestinationShelterId(),
                trace.getResourceType(),
                trace.getResourceName(),
                trace.getQuantity(),
                trace.getRecordedAt()
        );
    }
}

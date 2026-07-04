package com.drms.sharingservice.service;

import com.drms.sharingservice.client.ResourceServiceClient;
import com.drms.sharingservice.client.ShelterServiceClient;
import com.drms.sharingservice.dto.ExternalExcessStockView;
import com.drms.sharingservice.dto.ExternalReservationRequest;
import com.drms.sharingservice.dto.ExternalShortageAnalysisRequest;
import com.drms.sharingservice.dto.ExternalShortageAnalysisResponse;
import com.drms.sharingservice.dto.ExternalStockReservationResponse;
import com.drms.sharingservice.dto.ExternalTransferConfirmRequest;
import com.drms.sharingservice.dto.DonationTraceSummaryResponse;
import com.drms.sharingservice.dto.ShortageRequestDto;
import com.drms.sharingservice.dto.ShortageRequestResponse;
import com.drms.sharingservice.dto.TransferResponse;
import com.drms.sharingservice.dto.TransparencyView;
import com.drms.sharingservice.entity.DonationTrace;
import com.drms.sharingservice.entity.ShortageRequest;
import com.drms.sharingservice.entity.ShortageRequestStatus;
import com.drms.sharingservice.entity.Transfer;
import com.drms.sharingservice.entity.TransferStatus;
import com.drms.sharingservice.entity.TransparencyTimelineEvent;
import com.drms.sharingservice.exception.ConflictException;
import com.drms.sharingservice.exception.NotFoundException;
import com.drms.sharingservice.mapper.SharingMapper;
import com.drms.sharingservice.repository.DonationTraceRepository;
import com.drms.sharingservice.repository.ShortageRequestRepository;
import com.drms.sharingservice.repository.TransferRepository;
import com.drms.sharingservice.repository.TransparencyTimelineEventRepository;
import com.drms.sharingservice.dto.AdminTransferRequest;
import com.drms.sharingservice.dto.ExcessNotificationRequest;
import com.drms.sharingservice.dto.BulkExcessNotificationRequest;
import com.drms.sharingservice.dto.ExcessRequestRequest;
import com.drms.sharingservice.entity.ExcessNotification;
import com.drms.sharingservice.entity.ExcessRequest;
import com.drms.sharingservice.repository.ExcessNotificationRepository;
import com.drms.sharingservice.repository.ExcessRequestRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SharingTransparencyService {

    private final ShortageRequestRepository shortageRepository;
    private final TransferRepository transferRepository;
    private final DonationTraceRepository donationTraceRepository;
    private final TransparencyTimelineEventRepository eventRepository;
    private final ExcessNotificationRepository excessNotificationRepository;
    private final ExcessRequestRepository excessRequestRepository;
    private final SharingMapper sharingMapper;
    private final ShelterServiceClient shelterServiceClient;
    private final ResourceServiceClient resourceServiceClient;
    private final EventPublisherService eventPublisherService;

    public SharingTransparencyService(
            ShortageRequestRepository shortageRepository,
            TransferRepository transferRepository,
            DonationTraceRepository donationTraceRepository,
            TransparencyTimelineEventRepository eventRepository,
            ExcessNotificationRepository excessNotificationRepository,
            ExcessRequestRepository excessRequestRepository,
            SharingMapper sharingMapper,
            ShelterServiceClient shelterServiceClient,
            ResourceServiceClient resourceServiceClient,
            EventPublisherService eventPublisherService
    ) {
        this.shortageRepository = shortageRepository;
        this.transferRepository = transferRepository;
        this.donationTraceRepository = donationTraceRepository;
        this.eventRepository = eventRepository;
        this.excessNotificationRepository = excessNotificationRepository;
        this.excessRequestRepository = excessRequestRepository;
        this.sharingMapper = sharingMapper;
        this.shelterServiceClient = shelterServiceClient;
        this.resourceServiceClient = resourceServiceClient;
        this.eventPublisherService = eventPublisherService;
    }

    @Transactional
    public ShortageRequestResponse createShortage(ShortageRequestDto request) {
        validateShelterAvailability(request.shelterId());
        ExternalShortageAnalysisResponse analysis = resourceServiceClient.analyzeShortage(
                new ExternalShortageAnalysisRequest(
                        request.shelterId(),
                        request.resourceType(),
                        request.resourceName(),
                        request.requiredQuantity()
                )
        );
        if (!analysis.shortage()) {
            throw new ConflictException("Shelter does not currently have a shortage for this resource");
        }

        ShortageRequest shortage = shortageRepository.save(ShortageRequest.builder()
                .shelterId(request.shelterId())
                .resourceType(request.resourceType())
                .resourceName(request.resourceName())
                .unit(request.unit())
                .requiredQuantity(request.requiredQuantity())
                .shortageQuantity(analysis.shortageQuantity())
                .justification(request.justification())
                .status(ShortageRequestStatus.OPEN)
                .build());
        eventPublisherService.publish("shortage.created", Map.of(
                "shortageRequestId", shortage.getId(),
                "shelterId", shortage.getShelterId(),
                "resourceName", shortage.getResourceName()
        ));
        recordTimeline("SHORTAGE_CREATED", "Shortage request submitted by shelter " + shortage.getShelterId(), null, null);
        return sharingMapper.toShortageResponse(shortage);
    }

    public ShortageRequestResponse getShortage(Long id) {
        return sharingMapper.toShortageResponse(getShortageEntity(id));
    }

    public List<ShortageRequestResponse> listShortages(Long shelterId) {
        List<ShortageRequest> shortages = shelterId == null
                ? shortageRepository.findAllByOrderByCreatedAtDesc()
                : shortageRepository.findByShelterIdOrderByCreatedAtDesc(shelterId);
        return shortages.stream()
                .map(sharingMapper::toShortageResponse)
                .toList();
    }

    @Transactional
    public TransferResponse matchShortage(Long shortageRequestId) {
        ShortageRequest shortage = getShortageEntity(shortageRequestId);
        if (shortage.getStatus() != ShortageRequestStatus.OPEN) {
            throw new ConflictException("Shortage request is not open for matching");
        }

        List<ExternalExcessStockView> candidates = resourceServiceClient.getExcess(
                shortage.getResourceType(),
                shortage.getResourceName(),
                shortage.getShortageQuantity()
        );
        ExternalExcessStockView match = candidates.stream()
                .filter(candidate -> !candidate.shelterId().equals(shortage.getShelterId()))
                .findFirst()
                .orElseThrow(() -> new ConflictException("No matching shelter inventory found"));

        ExternalStockReservationResponse reservation = resourceServiceClient.reserve(
                new ExternalReservationRequest(
                        match.shelterId(),
                        shortage.getShelterId(),
                        shortage.getResourceType(),
                        shortage.getResourceName(),
                        shortage.getUnit(),
                        shortage.getShortageQuantity(),
                        "SHORTAGE-" + shortage.getId()
                )
        );

        shortage.setStatus(ShortageRequestStatus.MATCHED);
        Transfer transfer = transferRepository.save(Transfer.builder()
                .shortageRequestId(shortage.getId())
                .sourceShelterId(match.shelterId())
                .targetShelterId(shortage.getShelterId())
                .reservationId(reservation.reservationId())
                .sourceBatchId(match.batchId())
                .donationRef(match.sourceDonationRef())
                .resourceType(shortage.getResourceType())
                .resourceName(shortage.getResourceName())
                .unit(shortage.getUnit())
                .quantity(shortage.getShortageQuantity())
                .status(TransferStatus.RESERVED)
                .build());
        recordTimeline("MATCH_CREATED", "Reserved stock from shelter " + match.shelterId(), match.sourceDonationRef(), transfer.getId());
        return sharingMapper.toTransferResponse(transfer);
    }

    @Transactional
    public TransferResponse dispatch(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.RESERVED) {
            throw new ConflictException("Only reserved transfers can be dispatched");
        }
        transfer.setStatus(TransferStatus.DISPATCHED);
        recordTimeline("TRANSFER_DISPATCHED", "Transfer dispatched from shelter " + transfer.getSourceShelterId(), transfer.getDonationRef(), transfer.getId());
        return sharingMapper.toTransferResponse(transfer);
    }

    @Transactional
    public TransferResponse receive(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.DISPATCHED) {
            throw new ConflictException("Only dispatched transfers can be received");
        }

        resourceServiceClient.confirmTransfer(new ExternalTransferConfirmRequest(transfer.getReservationId(), transfer.getTargetShelterId()));
        transfer.setStatus(TransferStatus.COMPLETED);

        ShortageRequest shortage = getShortageEntity(transfer.getShortageRequestId());
        shortage.setStatus(ShortageRequestStatus.FULFILLED);

        donationTraceRepository.save(DonationTrace.builder()
                .donationRef(transfer.getDonationRef())
                .transferId(transfer.getId())
                .sourceShelterId(transfer.getSourceShelterId())
                .destinationShelterId(transfer.getTargetShelterId())
                .resourceType(transfer.getResourceType())
                .resourceName(transfer.getResourceName())
                .quantity(transfer.getQuantity())
                .recordedAt(Instant.now())
                .build());
        recordTimeline("TRANSFER_COMPLETED", "Transfer received by shelter " + transfer.getTargetShelterId(), transfer.getDonationRef(), transfer.getId());
        eventPublisherService.publish("donation.logged", Map.of(
                "transferId", transfer.getId(),
                "donationRef", transfer.getDonationRef(),
                "destinationShelterId", transfer.getTargetShelterId()
        ));
        return sharingMapper.toTransferResponse(transfer);
    }

    @Transactional
    public TransferResponse cancel(Long transferId) {
        Transfer transfer = getTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.RESERVED) {
            throw new ConflictException("Only reserved transfers can be cancelled");
        }
        resourceServiceClient.release(transfer.getReservationId(), new ResourceServiceClient.ReleaseReason("Transfer cancelled"));
        transfer.setStatus(TransferStatus.CANCELLED);

        ShortageRequest shortage = getShortageEntity(transfer.getShortageRequestId());
        shortage.setStatus(ShortageRequestStatus.OPEN);
        recordTimeline("TRANSFER_CANCELLED", "Transfer reservation cancelled", transfer.getDonationRef(), transfer.getId());
        return sharingMapper.toTransferResponse(transfer);
    }

    public List<TransferResponse> listTransfers(Long shelterId, String direction) {
        List<Transfer> transfers = switch (direction == null ? "all" : direction.toLowerCase()) {
            case "incoming" -> transferRepository.findByTargetShelterIdOrderByCreatedAtDesc(shelterId);
            case "outgoing" -> transferRepository.findBySourceShelterIdOrderByCreatedAtDesc(shelterId);
            default -> {
                List<Transfer> incoming = transferRepository.findByTargetShelterIdOrderByCreatedAtDesc(shelterId);
                List<Transfer> outgoing = transferRepository.findBySourceShelterIdOrderByCreatedAtDesc(shelterId);
                yield java.util.stream.Stream.concat(incoming.stream(), outgoing.stream())
                        .sorted(java.util.Comparator.comparing(Transfer::getCreatedAt).reversed())
                        .toList();
            }
        };
        return transfers.stream().map(sharingMapper::toTransferResponse).toList();
    }

    public TransparencyView getTransparency(String donationRef) {
        DonationTrace trace = donationTraceRepository.findFirstByDonationRef(donationRef)
                .orElseThrow(() -> new NotFoundException("Donation trace not found"));
        return new TransparencyView(
                trace.getDonationRef(),
                trace.getResourceType(),
                trace.getResourceName(),
                trace.getSourceShelterId(),
                trace.getDestinationShelterId(),
                trace.getQuantity(),
                eventRepository.findByDonationRefOrderByOccurredAtAsc(donationRef).stream()
                        .map(sharingMapper::toTimelineEvent)
                        .toList()
        );
    }

    public List<DonationTraceSummaryResponse> getAllTraces() {
        return donationTraceRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(DonationTrace::getRecordedAt).reversed())
                .map(sharingMapper::toDonationTraceSummary)
                .toList();
    }

    private void validateShelterAvailability(Long shelterId) {
        if (!shelterServiceClient.isAvailable(shelterId)) {
            throw new ConflictException("Shelter is not active and cannot participate");
        }
    }

    private ShortageRequest getShortageEntity(Long id) {
        return shortageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shortage request not found"));
    }

    private Transfer getTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer not found"));
    }

    private void recordTimeline(String type, String details, String donationRef, Long transferId) {
        if (donationRef == null) {
            return;
        }
        eventRepository.save(TransparencyTimelineEvent.builder()
                .donationRef(donationRef)
                .transferId(transferId)
                .eventType(type)
                .details(details)
                .build());
    }

    @Transactional
    public TransferResponse createAdminTransfer(AdminTransferRequest request) {
        validateShelterAvailability(request.targetShelterId());

        ExternalStockReservationResponse reservation = resourceServiceClient.reserve(
                new ExternalReservationRequest(
                        0L,
                        request.targetShelterId(),
                        request.resourceType(),
                        request.resourceName(),
                        request.unit(),
                        request.quantity(),
                        "ADMIN-MANUAL-" + System.currentTimeMillis(),
                        request.batchId()
                )
        );

        if (request.shortageRequestId() != null) {
            ShortageRequest shortage = getShortageEntity(request.shortageRequestId());
            shortage.setStatus(ShortageRequestStatus.MATCHED);
            shortageRepository.save(shortage);
        }

        Transfer transfer = transferRepository.save(Transfer.builder()
                .shortageRequestId(request.shortageRequestId())
                .sourceShelterId(0L)
                .targetShelterId(request.targetShelterId())
                .reservationId(reservation.reservationId())
                .sourceBatchId(request.batchId())
                .donationRef(request.donationRef())
                .resourceType(request.resourceType())
                .resourceName(request.resourceName())
                .unit(request.unit())
                .quantity(request.quantity())
                .status(TransferStatus.DISPATCHED)
                .build());

        recordTimeline("TRANSFER_DISPATCHED", "Transfer dispatched from Admin to shelter " + request.targetShelterId(), request.donationRef(), transfer.getId());
        return sharingMapper.toTransferResponse(transfer);
    }

    @Transactional
    public ExcessNotification createExcess(ExcessNotificationRequest request) {
        validateShelterAvailability(request.shelterId());
        ExcessNotification excess = ExcessNotification.builder()
                .shelterId(request.shelterId())
                .batchId(request.batchId())
                .sourceDonationRef(request.sourceDonationRef())
                .resourceType(request.resourceType())
                .resourceName(request.resourceName())
                .unit(request.unit())
                .quantity(request.quantity())
                .status("OPEN")
                .build();
        return excessNotificationRepository.save(excess);
    }

    @Transactional
    public List<ExcessNotification> createBulkExcess(BulkExcessNotificationRequest request) {
        validateShelterAvailability(request.shelterId());
        return request.items().stream()
                .map(item -> ExcessNotification.builder()
                        .shelterId(request.shelterId())
                        .batchId(item.batchId())
                        .sourceDonationRef(item.sourceDonationRef())
                        .resourceType(item.resourceType())
                        .resourceName(item.resourceName())
                        .unit(item.unit())
                        .quantity(item.quantity())
                        .status("OPEN")
                        .build())
                .map(excessNotificationRepository::save)
                .toList();
    }

    public List<ExcessNotification> listExcess(Long shelterId) {
        if (shelterId != null) {
            return excessNotificationRepository.findByShelterIdOrderByCreatedAtDesc(shelterId);
        }
        return excessNotificationRepository.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    @Transactional
    public ExcessRequest createExcessRequest(Long notificationId, ExcessRequestRequest request) {
        validateShelterAvailability(request.requestingShelterId());
        ExcessNotification excess = excessNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Excess notification not found"));
        if (!"OPEN".equals(excess.getStatus())) {
            throw new ConflictException("Excess notification is no longer open");
        }
        if (request.quantity() > excess.getQuantity()) {
            throw new ConflictException("Requested quantity exceeds available excess quantity");
        }
        ExcessRequest req = ExcessRequest.builder()
                .excessNotificationId(notificationId)
                .requestingShelterId(request.requestingShelterId())
                .quantity(request.quantity())
                .status("PENDING")
                .build();
        return excessRequestRepository.save(req);
    }

    public List<ExcessRequest> listRequestsForNotification(Long notificationId) {
        return excessRequestRepository.findByExcessNotificationId(notificationId);
    }

    @Transactional
    public TransferResponse approveExcessRequest(Long requestId) {
        ExcessRequest req = excessRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Excess request not found"));
        if (!"PENDING".equals(req.getStatus())) {
            throw new ConflictException("Request is not pending approval");
        }

        ExcessNotification excess = excessNotificationRepository.findById(req.getExcessNotificationId())
                .orElseThrow(() -> new NotFoundException("Associated excess notification not found"));
        if (!"OPEN".equals(excess.getStatus())) {
            throw new ConflictException("Notification is no longer open");
        }
        if (excess.getQuantity() < req.getQuantity()) {
            throw new ConflictException("No longer enough excess quantity available to satisfy request");
        }

        Long targetBatchId = excess.getBatchId();
        String targetDonationRef = excess.getSourceDonationRef();
        if (targetBatchId == null || targetDonationRef == null) {
            List<ExternalExcessStockView> batches = resourceServiceClient.getExcess(
                    excess.getResourceType(),
                    excess.getResourceName(),
                    req.getQuantity()
            );
            ExternalExcessStockView match = batches.stream()
                    .filter(b -> b.shelterId().equals(excess.getShelterId()))
                    .findFirst()
                    .orElseThrow(() -> new ConflictException("No matching inventory batch found at source shelter"));
            targetBatchId = match.batchId();
            targetDonationRef = match.sourceDonationRef();
        }

        ExternalStockReservationResponse reservation = resourceServiceClient.reserve(
                new ExternalReservationRequest(
                        excess.getShelterId(),
                        req.getRequestingShelterId(),
                        excess.getResourceType(),
                        excess.getResourceName(),
                        excess.getUnit(),
                        req.getQuantity(),
                        "EXCESS-REQ-" + req.getId(),
                        targetBatchId
                )
        );

        excess.setQuantity(excess.getQuantity() - req.getQuantity());
        if (excess.getQuantity() <= 0) {
            excess.setStatus("RESOLVED");
        }
        excessNotificationRepository.save(excess);

        req.setStatus("APPROVED");
        excessRequestRepository.save(req);

        List<ExcessRequest> otherRequests = excessRequestRepository.findByExcessNotificationId(excess.getId());
        for (ExcessRequest other : otherRequests) {
            if ("PENDING".equals(other.getStatus()) && other.getQuantity() > excess.getQuantity()) {
                other.setStatus("REJECTED");
                excessRequestRepository.save(other);
            }
        }

        Transfer transfer = transferRepository.save(Transfer.builder()
                .shortageRequestId(null)
                .sourceShelterId(excess.getShelterId())
                .targetShelterId(req.getRequestingShelterId())
                .reservationId(reservation.reservationId())
                .sourceBatchId(targetBatchId)
                .donationRef(targetDonationRef)
                .resourceType(excess.getResourceType())
                .resourceName(excess.getResourceName())
                .unit(excess.getUnit())
                .quantity(req.getQuantity())
                .status(TransferStatus.DISPATCHED)
                .build());

        recordTimeline("TRANSFER_DISPATCHED", "Transfer dispatched from shelter " + excess.getShelterId() + " to shelter " + req.getRequestingShelterId(), targetDonationRef, transfer.getId());
        return sharingMapper.toTransferResponse(transfer);
    }

    @Transactional
    public void rejectExcessRequest(Long requestId) {
        ExcessRequest req = excessRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Excess request not found"));
        if (!"PENDING".equals(req.getStatus())) {
            throw new ConflictException("Request is not pending approval");
        }
        req.setStatus("REJECTED");
        excessRequestRepository.save(req);
    }
}

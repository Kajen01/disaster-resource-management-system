package com.drms.sharingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.drms.sharingservice.client.ResourceServiceClient;
import com.drms.sharingservice.client.ShelterServiceClient;
import com.drms.sharingservice.dto.ExternalExcessStockView;
import com.drms.sharingservice.dto.ExternalStockReservationResponse;
import com.drms.sharingservice.dto.ShortageRequestDto;
import com.drms.sharingservice.dto.ShortageRequestResponse;
import com.drms.sharingservice.dto.TransferResponse;
import com.drms.sharingservice.entity.ShortageRequest;
import com.drms.sharingservice.entity.ShortageRequestStatus;
import com.drms.sharingservice.entity.Transfer;
import com.drms.sharingservice.entity.TransferStatus;
import com.drms.sharingservice.exception.ConflictException;
import com.drms.sharingservice.mapper.SharingMapper;
import com.drms.sharingservice.repository.DonationTraceRepository;
import com.drms.sharingservice.repository.ShortageRequestRepository;
import com.drms.sharingservice.repository.TransferRepository;
import com.drms.sharingservice.repository.TransparencyTimelineEventRepository;
import com.drms.sharingservice.service.EventPublisherService;
import com.drms.sharingservice.service.SharingTransparencyService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SharingTransparencyServiceTest {

    @Mock
    private ShortageRequestRepository shortageRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private DonationTraceRepository donationTraceRepository;

    @Mock
    private TransparencyTimelineEventRepository timelineRepository;

    @Spy
    private SharingMapper sharingMapper;

    @Mock
    private ShelterServiceClient shelterServiceClient;

    @Mock
    private ResourceServiceClient resourceServiceClient;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private SharingTransparencyService sharingTransparencyService;

    @Test
    void createShortageRejectsWhenNoRealShortageExists() {
        when(shelterServiceClient.isAvailable(2L)).thenReturn(true);
        when(resourceServiceClient.analyzeShortage(any()))
                .thenReturn(new com.drms.sharingservice.dto.ExternalShortageAnalysisResponse(2L, "Water", 50, 50, 0, false));

        ShortageRequestDto request = new ShortageRequestDto(2L, "WATER", "Water", "bottles", 50, "Storm response");

        assertThrows(ConflictException.class, () -> sharingTransparencyService.createShortage(request));
    }

    @Test
    void matchShortageCreatesReservedTransfer() {
        ShortageRequest shortage = ShortageRequest.builder()
                .id(10L)
                .shelterId(2L)
                .resourceType("FOOD")
                .resourceName("Dry Rations")
                .unit("packs")
                .shortageQuantity(60)
                .status(ShortageRequestStatus.OPEN)
                .build();

        when(shortageRepository.findById(10L)).thenReturn(Optional.of(shortage));
        when(resourceServiceClient.getExcess("FOOD", "Dry Rations", 60))
                .thenReturn(List.of(new ExternalExcessStockView(5L, 1L, "FOOD", "Dry Rations", "packs", 100, null, "DON-001")));
        when(resourceServiceClient.reserve(any()))
                .thenReturn(new ExternalStockReservationResponse(33L, 5L, 1L, 2L, "FOOD", "Dry Rations", "packs", 60, "SHORTAGE-10", "RESERVED"));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(77L);
            return transfer;
        });

        TransferResponse response = sharingTransparencyService.matchShortage(10L);

        assertEquals("MATCHED", shortage.getStatus().name());
        assertEquals("RESERVED", response.status());
        assertEquals(1L, response.sourceShelterId());
        assertEquals(2L, response.targetShelterId());
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void receiveTransferCompletesTransferAndFulfillShortage() {
        Transfer transfer = Transfer.builder()
                .id(77L)
                .shortageRequestId(10L)
                .sourceShelterId(1L)
                .targetShelterId(2L)
                .reservationId(33L)
                .donationRef("DON-001")
                .resourceType("FOOD")
                .resourceName("Dry Rations")
                .unit("packs")
                .quantity(60)
                .status(TransferStatus.DISPATCHED)
                .build();
        ShortageRequest shortage = ShortageRequest.builder()
                .id(10L)
                .status(ShortageRequestStatus.MATCHED)
                .build();

        when(transferRepository.findById(77L)).thenReturn(Optional.of(transfer));
        when(shortageRepository.findById(10L)).thenReturn(Optional.of(shortage));

        TransferResponse response = sharingTransparencyService.receive(77L);

        assertEquals("COMPLETED", response.status());
        assertEquals(TransferStatus.COMPLETED, transfer.getStatus());
        assertEquals(ShortageRequestStatus.FULFILLED, shortage.getStatus());
        verify(resourceServiceClient).confirmTransfer(any());
        verify(donationTraceRepository).save(any());
    }

    @Test
    void cancelTransferReopensShortageAndReleasesReservation() {
        Transfer transfer = Transfer.builder()
                .id(88L)
                .shortageRequestId(12L)
                .reservationId(44L)
                .donationRef("DON-008")
                .status(TransferStatus.RESERVED)
                .build();
        ShortageRequest shortage = ShortageRequest.builder()
                .id(12L)
                .status(ShortageRequestStatus.MATCHED)
                .build();

        when(transferRepository.findById(88L)).thenReturn(Optional.of(transfer));
        when(shortageRepository.findById(12L)).thenReturn(Optional.of(shortage));

        TransferResponse response = sharingTransparencyService.cancel(88L);

        assertEquals("CANCELLED", response.status());
        assertEquals(TransferStatus.CANCELLED, transfer.getStatus());
        assertEquals(ShortageRequestStatus.OPEN, shortage.getStatus());
        verify(resourceServiceClient).release(44L, new ResourceServiceClient.ReleaseReason("Transfer cancelled"));
    }
}

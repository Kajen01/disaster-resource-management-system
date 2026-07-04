package com.drms.resourceservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.drms.resourceservice.dto.ReservationRequest;
import com.drms.resourceservice.dto.BatchIntakeRequest;
import com.drms.resourceservice.dto.BatchResponse;
import com.drms.resourceservice.dto.ShortageAnalysisRequest;
import com.drms.resourceservice.dto.ShortageAnalysisResponse;
import com.drms.resourceservice.dto.StockReservationResponse;
import com.drms.resourceservice.dto.TransferConfirmRequest;
import com.drms.resourceservice.entity.ReservationStatus;
import com.drms.resourceservice.entity.ResourceBatch;
import com.drms.resourceservice.entity.ResourceCategory;
import com.drms.resourceservice.entity.StockReservation;
import com.drms.resourceservice.mapper.ResourceMapper;
import com.drms.resourceservice.repository.ResourceBatchRepository;
import com.drms.resourceservice.repository.StockReservationRepository;
import com.drms.resourceservice.service.EventPublisherService;
import com.drms.resourceservice.service.ResourceInventoryService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceInventoryServiceTest {

    @Mock
    private ResourceBatchRepository batchRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private ResourceInventoryService inventoryService;

    @Test
    void analyzeShortageReturnsMissingQuantity() {
        when(batchRepository.findByShelterIdAndResourceNameIgnoreCase(1L, "Dry Rations"))
                .thenReturn(List.of(
                        ResourceBatch.builder().resourceType(ResourceCategory.FOOD).quantityAvailable(20).build(),
                        ResourceBatch.builder().resourceType(ResourceCategory.FOOD).quantityAvailable(10).build()
                ));

        ShortageAnalysisResponse response = inventoryService.analyzeShortage(
                new ShortageAnalysisRequest(1L, ResourceCategory.FOOD, "Dry Rations", 50)
        );

        assertEquals(30, response.availableQuantity());
        assertEquals(20, response.shortageQuantity());
        assertTrue(response.shortage());
    }

    @Test
    void intakeGeneratesDonationReferenceAutomatically() {
        ResourceBatch savedBatch = ResourceBatch.builder()
                .id(3L)
                .shelterId(1L)
                .donorEmail("donor@example.com")
                .resourceType(ResourceCategory.WATER)
                .resourceName("Bottled Water")
                .unit("bottles")
                .quantityReceived(24)
                .quantityAvailable(24)
                .sourceDonationRef("DON-20260518-ABC123")
                .build();

        when(batchRepository.save(any(ResourceBatch.class))).thenReturn(savedBatch);
        when(resourceMapper.toBatchResponse(savedBatch))
                .thenReturn(new BatchResponse(
                        3L,
                        1L,
                        "donor@example.com",
                        ResourceCategory.WATER,
                        "Bottled Water",
                        "bottles",
                        24,
                        24,
                        null,
                        null,
                        "DON-20260518-ABC123"
                ));

        BatchResponse response = inventoryService.intake(
                new BatchIntakeRequest(1L, ResourceCategory.WATER, "Bottled Water", "bottles", 24, null, null),
                "donor@example.com"
        );

        assertNotNull(response.sourceDonationRef());
        assertTrue(response.sourceDonationRef().startsWith("DON-"));
        verify(eventPublisherService).publish(any(), any());
    }

    @Test
    void reserveSelectsEarliestExpiryBatchAndReducesAvailableQuantity() {
        ResourceBatch laterExpiry = ResourceBatch.builder()
                .id(10L)
                .shelterId(1L)
                .resourceType(ResourceCategory.FOOD)
                .resourceName("Dry Rations")
                .unit("packs")
                .quantityAvailable(100)
                .expiryDate(LocalDate.now().plusDays(10))
                .sourceDonationRef("DON-010")
                .build();
        ResourceBatch earlierExpiry = ResourceBatch.builder()
                .id(11L)
                .shelterId(1L)
                .resourceType(ResourceCategory.FOOD)
                .resourceName("Dry Rations")
                .unit("packs")
                .quantityAvailable(90)
                .expiryDate(LocalDate.now().plusDays(2))
                .sourceDonationRef("DON-011")
                .build();
        StockReservation savedReservation = StockReservation.builder()
                .id(55L)
                .batchId(11L)
                .sourceShelterId(1L)
                .targetShelterId(2L)
                .resourceType(ResourceCategory.FOOD)
                .resourceName("Dry Rations")
                .unit("packs")
                .reservedQuantity(40)
                .referenceNumber("SHORTAGE-11")
                .status(ReservationStatus.RESERVED)
                .build();

        when(batchRepository.findAll()).thenReturn(List.of(laterExpiry, earlierExpiry));
        when(reservationRepository.save(any(StockReservation.class))).thenReturn(savedReservation);
        when(resourceMapper.toReservationResponse(savedReservation))
                .thenReturn(new StockReservationResponse(55L, 11L, 1L, 2L, ResourceCategory.FOOD, "Dry Rations", "packs", 40, "SHORTAGE-11", ReservationStatus.RESERVED, null));

        StockReservationResponse response = inventoryService.reserve(
                new ReservationRequest(1L, 2L, ResourceCategory.FOOD, "Dry Rations", "packs", 40, "SHORTAGE-11")
        );

        assertEquals(50, earlierExpiry.getQuantityAvailable());
        assertEquals(100, laterExpiry.getQuantityAvailable());
        assertEquals(11L, response.batchId());
        verify(eventPublisherService).publish(any(), any());
    }

    @Test
    void releaseRestoresBatchQuantityAndMarksReservationReleased() {
        ResourceBatch batch = ResourceBatch.builder()
                .id(11L)
                .quantityAvailable(50)
                .build();
        StockReservation reservation = StockReservation.builder()
                .id(55L)
                .batchId(11L)
                .reservedQuantity(40)
                .status(ReservationStatus.RESERVED)
                .build();

        when(reservationRepository.findById(55L)).thenReturn(java.util.Optional.of(reservation));
        when(batchRepository.findById(11L)).thenReturn(java.util.Optional.of(batch));
        when(resourceMapper.toReservationResponse(reservation))
                .thenReturn(new StockReservationResponse(55L, 11L, 1L, 2L, ResourceCategory.FOOD, "Dry Rations", "packs", 40, "SHORTAGE-11", ReservationStatus.RELEASED, null));

        StockReservationResponse response = inventoryService.release(55L, "Transfer cancelled");

        assertEquals(90, batch.getQuantityAvailable());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        assertEquals(ReservationStatus.RELEASED, response.status());
    }

    @Test
    void confirmTransferCreatesDestinationBatchAndCompletesReservation() {
        ResourceBatch sourceBatch = ResourceBatch.builder()
                .id(11L)
                .resourceType(ResourceCategory.MEDICINE)
                .resourceName("Bandages")
                .unit("boxes")
                .quantityAvailable(60)
                .expiryDate(LocalDate.now().plusDays(30))
                .sourceDonationRef("DON-200")
                .build();
        StockReservation reservation = StockReservation.builder()
                .id(55L)
                .batchId(11L)
                .sourceShelterId(1L)
                .targetShelterId(2L)
                .resourceType(ResourceCategory.MEDICINE)
                .resourceName("Bandages")
                .unit("boxes")
                .reservedQuantity(25)
                .referenceNumber("SHORTAGE-21")
                .status(ReservationStatus.RESERVED)
                .build();

        when(reservationRepository.findById(55L)).thenReturn(java.util.Optional.of(reservation));
        when(batchRepository.findById(11L)).thenReturn(java.util.Optional.of(sourceBatch));
        when(batchRepository.save(any(ResourceBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(resourceMapper.toReservationResponse(reservation))
                .thenReturn(new StockReservationResponse(55L, 11L, 1L, 2L, ResourceCategory.MEDICINE, "Bandages", "boxes", 25, "SHORTAGE-21", ReservationStatus.COMPLETED, null));

        StockReservationResponse response = inventoryService.confirmTransfer(new TransferConfirmRequest(55L, 2L));

        ArgumentCaptor<ResourceBatch> destinationBatchCaptor = ArgumentCaptor.forClass(ResourceBatch.class);
        verify(batchRepository).save(destinationBatchCaptor.capture());
        ResourceBatch destinationBatch = destinationBatchCaptor.getValue();

        assertEquals(2L, destinationBatch.getShelterId());
        assertEquals("DON-200", destinationBatch.getSourceDonationRef());
        assertEquals(25, destinationBatch.getQuantityAvailable());
        assertEquals(ReservationStatus.COMPLETED, reservation.getStatus());
        assertEquals(ReservationStatus.COMPLETED, response.status());
        verify(eventPublisherService).publish(any(), any());
    }
}
